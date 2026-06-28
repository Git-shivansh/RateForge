// Ramp-up traffic: gradually increases from 5 to 50 virtual users over
// 20 seconds, then ramps back down. Simulates organic traffic growth,
// e.g. a marketing campaign driving increasing load over time.
//
// Run: k6 run k6/ramp-load.js

import http from 'k6/http';

export const options = {
    stages: [
        { duration: '10s', target: 5 },
        { duration: '20s', target: 50 },
        { duration: '10s', target: 0 },
    ],
};

export default function () {
    http.get('http://localhost:8080/api/ping', {
        headers: { 'X-User-Id': 'user-1' },
    });
}
