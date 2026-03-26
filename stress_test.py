#!/usr/bin/env python3
"""
Stress Test Script for Shop Management System
Tests high concurrency scenarios to find breaking points
"""

import argparse
import json
import statistics
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from typing import Dict, List
import requests


def percentile(sorted_values, percent):
    if not sorted_values:
        return 0.0
    index = (len(sorted_values) - 1) * percent
    lower = int(index)
    upper = min(lower + 1, len(sorted_values) - 1)
    weight = index - lower
    return sorted_values[lower] * (1 - weight) + sorted_values[upper] * weight


def send_request(session, method, url, headers, payload):
    started_at = time.perf_counter()
    try:
        response = session.request(
            method=method, url=url, headers=headers, json=payload, timeout=30
        )
        latency_ms = (time.perf_counter() - started_at) * 1000
        return response.status_code, latency_ms, response.text
    except requests.RequestException as e:
        latency_ms = (time.perf_counter() - started_at) * 1000
        return f"error_{type(e).__name__}", latency_ms, str(e)


def stress_test(url, total_requests, concurrency, method, headers, payload, name):
    print(f"\n{'=' * 60}")
    print(f"STRESS TEST: {name}")
    print(f"{'=' * 60}")
    print(f"Requests: {total_requests}, Concurrency: {concurrency}")

    status_counts = {}
    latencies = []
    errors = []
    started_at = time.perf_counter()

    with requests.Session() as session:
        with ThreadPoolExecutor(max_workers=concurrency) as executor:
            futures = [
                executor.submit(send_request, session, method, url, headers, payload)
                for _ in range(total_requests)
            ]

            for future in as_completed(futures):
                status_code, latency_ms, text = future.result()
                status_counts[status_code] = status_counts.get(status_code, 0) + 1
                latencies.append(latency_ms)
                if status_code >= 400 or "error" in str(status_code):
                    errors.append(f"{status_code}: {text[:50]}")

    duration = time.perf_counter() - started_at
    sorted_latencies = sorted(latencies)

    success_rate = (
        (total_requests - sum(1 for s in status_counts.keys() if s >= 400))
        / total_requests
    ) * 100

    result = {
        "name": name,
        "total_requests": total_requests,
        "concurrency": concurrency,
        "duration_seconds": round(duration, 2),
        "requests_per_second": round(total_requests / duration, 2),
        "status_counts": status_counts,
        "success_rate": round(success_rate, 1),
        "avg_latency_ms": round(statistics.mean(sorted_latencies), 2),
        "p50_latency_ms": round(percentile(sorted_latencies, 0.50), 2),
        "p95_latency_ms": round(percentile(sorted_latencies, 0.95), 2),
        "p99_latency_ms": round(percentile(sorted_latencies, 0.99), 2),
        "max_latency_ms": round(max(sorted_latencies), 2),
    }

    print(f"Duration: {result['duration_seconds']}s")
    print(f"Throughput: {result['requests_per_second']} req/s")
    print(f"Success Rate: {result['success_rate']}%")
    print(f"Avg Latency: {result['avg_latency_ms']}ms")
    print(f"P95 Latency: {result['p95_latency_ms']}ms")
    print(f"P99 Latency: {result['p99_latency_ms']}ms")
    print(f"Status Codes: {status_counts}")

    return result


def main():
    parser = argparse.ArgumentParser(description="Stress Test Shop Management System")
    parser.add_argument(
        "--url", default="http://localhost:8080/api", help="API Base URL"
    )
    parser.add_argument("--token", required=True, help="JWT Token")
    parser.add_argument(
        "--level", choices=["light", "medium", "heavy", "extreme"], default="medium"
    )
    args = parser.parse_args()

    headers = {
        "Authorization": f"Bearer {args.token}",
        "Content-Type": "application/json",
    }

    configs = {
        "light": [
            ("Read Products (100 req, 10 concurrent)", "GET", "/products", 100, 10, {}),
            (
                "Read Categories (100 req, 10 concurrent)",
                "GET",
                "/categories",
                100,
                10,
                {},
            ),
        ],
        "medium": [
            ("Read Products (500 req, 50 concurrent)", "GET", "/products", 500, 50, {}),
            (
                "Read Categories (500 req, 50 concurrent)",
                "GET",
                "/categories",
                500,
                50,
                {},
            ),
            (
                "Read by SKU (300 req, 30 concurrent)",
                "GET",
                "/products/sku/TEST-SKU-001",
                300,
                30,
                {},
            ),
        ],
        "heavy": [
            (
                "Read Products (1000 req, 100 concurrent)",
                "GET",
                "/products",
                1000,
                100,
                {},
            ),
            (
                "Read Categories (1000 req, 100 concurrent)",
                "GET",
                "/categories",
                1000,
                100,
                {},
            ),
            (
                "Read by ID (1000 req, 100 concurrent)",
                "GET",
                "/products/1",
                1000,
                100,
                {},
            ),
            (
                "Mixed Read/Write (500 req, 50 concurrent)",
                "POST",
                "/orders",
                500,
                50,
                {"items": [{"productId": 1, "quantity": 1}]},
            ),
        ],
        "extreme": [
            (
                "Max Read Products (2000 req, 200 concurrent)",
                "GET",
                "/products",
                2000,
                200,
                {},
            ),
            (
                "Max Read Categories (2000 req, 200 concurrent)",
                "GET",
                "/categories",
                2000,
                200,
                {},
            ),
            (
                "Sustained Load (3000 req, 150 concurrent)",
                "GET",
                "/suppliers",
                3000,
                150,
                {},
            ),
        ],
    }

    print("=" * 60)
    print("STRESS TEST: Shop Management System")
    print(f"Level: {args.level.upper()}")
    print("=" * 60)

    results = []
    for name, method, path, total, concurrent, payload in configs[args.level]:
        url = f"{args.url}{path}"
        result = stress_test(url, total, concurrent, method, headers, payload, name)
        results.append(result)

    # Summary
    print("\n" + "=" * 60)
    print("STRESS TEST SUMMARY")
    print("=" * 60)
    print(f"\n{'Test':<40} {'Req/s':>10} {'Success':>10} {'Avg ms':>10} {'P95 ms':>10}")
    print("-" * 80)
    for r in results:
        print(
            f"{r['name']:<40} {r['requests_per_second']:>10.1f} {r['success_rate']:>9.1f}% {r['avg_latency_ms']:>10.1f} {r['p95_latency_ms']:>10.1f}"
        )

    # Find breaking point
    failing = [r for r in results if r["success_rate"] < 95]
    if failing:
        print("\n⚠️  Breaking points detected:")
        for r in failing:
            print(f"  - {r['name']}: {r['success_rate']}% success rate")
    else:
        print("\n✅ All tests passed with >95% success rate")


if __name__ == "__main__":
    main()
