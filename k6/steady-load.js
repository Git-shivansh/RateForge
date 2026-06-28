// Steady traffic: 5 virtual users hitting the endpoint once per second
// for 30 seconds. Simulates normal, predictable usage.
//
// Run: k6 run k6/steady-load.js

import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
    vus: 5,
    duration: '30s',
};

export default function () {
    http.get('http://localhost:8080/api/ping', {
        headers: { 'X-User-Id': 'user-1' },
    });
    sleep(1);
}
