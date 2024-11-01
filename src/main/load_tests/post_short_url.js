import http from 'k6/http'
import {check, sleep} from 'k6'


let vus = 10100

function getRandomNumber(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

let randomNum = getRandomNumber(1, 6000);


export let options = {
    stages: [{duration: '10s', target: vus}, // ramp-up to 20 VUs over 30 seconds
        {duration: '60s', target: vus},  // stay at 20 VUs for 1 minute
        {duration: '10s', target: 0},  // ramp-down to 0 VUs over 30 seconds
    ], thresholds: {
        http_req_duration: ['p(95)<1000'], // 95% of requests should be below 500ms
        http_req_failed: ['rate<0.01'],   // error rate should be less than 1%
    },
};

let port = 8080

export default function () {
    let random = Math.random();
    let url = `http://0.0.0.0:${port}/shorten?url=https://hello.world/${random}`;
    let response = http.post(url);

    // Check that the response is successful
    check(response, {
        'status is 201': (r) => r.status === 201
    });

    sleep(1);
}
