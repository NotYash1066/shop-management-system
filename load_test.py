import time
import requests
import threading

URL = "http://localhost:8080/api/auth/login"  # Target endpoint
TOTAL_REQUESTS = 200
CONCURRENT_THREADS = 10

success_count = 0
rate_limited_count = 0
failed_count = 0
start_time = 0

def send_request():
    global success_count, rate_limited_count, failed_count
    try:
        # Sending a dummy request
        response = requests.post(URL, json={"username": "test", "password": "password"})
        if response.status_code == 200 or response.status_code == 401:
            # 401 is 'success' in terms of hitting the server logic, just bad creds
            success_count += 1
        elif response.status_code == 429:
            rate_limited_count += 1
            print(f"Rate limited! Retry-After: {response.headers.get('Retry-After')}")
        else:
            failed_count += 1
    except Exception as e:
        failed_count += 1

def load_test():
    global start_time
    print(f"Starting load test on {URL} with {TOTAL_REQUESTS} requests...")
    start_time = time.time()
    
    threads = []
    for _ in range(TOTAL_REQUESTS):
        t = threading.Thread(target=send_request)
        threads.append(t)
        t.start()
        # Small delay to not instantly crush local network stack
        time.sleep(0.005) 
        
    for t in threads:
        t.join()

    duration = time.time() - start_time
    print("\n--- Load Test Results ---")
    print(f"Total Requests: {TOTAL_REQUESTS}")
    print(f"Duration: {duration:.2f} seconds")
    print(f"RPS: {TOTAL_REQUESTS / duration:.2f}")
    print(f"Success/AuthFail (Under Limit): {success_count}")
    print(f"Rate Limited (429): {rate_limited_count}")
    print(f"Failed (ConnError): {failed_count}")

if __name__ == "__main__":
    load_test()
