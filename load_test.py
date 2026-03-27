"""
load_test.py — Simple concurrent HTTP load tester for the Shop Management System.

Fires a configurable number of requests at a single endpoint using a thread pool,
then prints a JSON summary with latency percentiles and status code counts.

Usage:
    python3 load_test.py --url <url> --method <GET|POST|...> \
        --requests <n> --concurrency <n> \
        --headers '<json>' --payload '<json>'

Example (authenticated GET):
    python3 load_test.py \\
        --url http://localhost:8080/api/products/sku/SKU-1 \\
        --method GET \\
        --requests 500 \\
        --concurrency 50 \\
        --headers '{"Authorization":"Bearer <jwt>"}' \\
        --payload '{}'

Output fields:
    url                  Target URL
    method               HTTP method used
    total_requests       Number of requests sent
    concurrency          Max parallel workers
    duration_seconds     Wall-clock time for the full run
    requests_per_second  Throughput (total_requests / duration_seconds)
    status_counts        Map of HTTP status code -> count (errors shown as "connection_error")
    avg_latency_ms       Mean end-to-end latency in milliseconds
    p50_latency_ms       Median latency
    p95_latency_ms       95th percentile latency
    p99_latency_ms       99th percentile latency
    max_latency_ms       Worst single request latency
"""

import argparse
import json
import statistics
import time
from concurrent.futures import ThreadPoolExecutor, as_completed

import requests


def percentile(sorted_values, percent):
    """Return the value at the given percentile (0.0–1.0) using linear interpolation."""
    if not sorted_values:
        return 0.0
    index = (len(sorted_values) - 1) * percent
    lower = int(index)
    upper = min(lower + 1, len(sorted_values) - 1)
    weight = index - lower
    return sorted_values[lower] * (1 - weight) + sorted_values[upper] * weight


def send_request(session, method, url, headers, payload):
    """Send a single HTTP request and return (status_code, latency_ms).

    Network/timeout errors are returned as ("connection_error", latency_ms)
    so they still contribute to latency stats without crashing the run.
    """
    started_at = time.perf_counter()
    try:
        response = session.request(method=method, url=url, headers=headers, json=payload, timeout=10)
        latency_ms = (time.perf_counter() - started_at) * 1000
        return response.status_code, latency_ms
    except requests.RequestException:
        latency_ms = (time.perf_counter() - started_at) * 1000
        return "connection_error", latency_ms


def run_load_test(url, total_requests, concurrency, method, payload, headers):
    """Fire total_requests against url using a thread pool of size concurrency.

    Returns a dict with throughput, status code distribution, and latency
    percentiles (avg, p50, p95, p99, max).
    """
    status_counts = {}
    latencies = []
    started_at = time.perf_counter()

    with requests.Session() as session:
        with ThreadPoolExecutor(max_workers=concurrency) as executor:
            futures = [
                executor.submit(send_request, session, method, url, headers, payload)
                for _ in range(total_requests)
            ]

            for future in as_completed(futures):
                status_code, latency_ms = future.result()
                status_counts[status_code] = status_counts.get(status_code, 0) + 1
                latencies.append(latency_ms)

    duration_seconds = time.perf_counter() - started_at
    sorted_latencies = sorted(latencies)
    summary = {
        "url": url,
        "method": method,
        "total_requests": total_requests,
        "concurrency": concurrency,
        "duration_seconds": round(duration_seconds, 2),
        "requests_per_second": round(total_requests / duration_seconds, 2) if duration_seconds else 0,
        "status_counts": status_counts,
        "avg_latency_ms": round(statistics.mean(sorted_latencies), 2) if sorted_latencies else 0,
        "p50_latency_ms": round(percentile(sorted_latencies, 0.50), 2),
        "p95_latency_ms": round(percentile(sorted_latencies, 0.95), 2),
        "p99_latency_ms": round(percentile(sorted_latencies, 0.99), 2),
        "max_latency_ms": round(max(sorted_latencies), 2) if sorted_latencies else 0,
    }
    return summary


def main():
    parser = argparse.ArgumentParser(description="Simple concurrent load test runner for Shop Management System APIs")
    parser.add_argument("--url", default="http://localhost:8080/api/auth/login")
    parser.add_argument("--method", default="POST")
    parser.add_argument("--requests", type=int, default=200)
    parser.add_argument("--concurrency", type=int, default=20)
    parser.add_argument(
        "--payload",
        default='{"username":"test","password":"password"}',
        help="JSON payload string for request bodies",
    )
    parser.add_argument(
        "--headers",
        default='{"Content-Type":"application/json"}',
        help="JSON object string for request headers",
    )
    args = parser.parse_args()

    payload = json.loads(args.payload)
    headers = json.loads(args.headers)

    summary = run_load_test(
        url=args.url,
        total_requests=args.requests,
        concurrency=args.concurrency,
        method=args.method.upper(),
        payload=payload,
        headers=headers,
    )

    print(json.dumps(summary, indent=2))


if __name__ == "__main__":
    main()
