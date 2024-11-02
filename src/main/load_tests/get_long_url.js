import http from 'k6/http'
import {check, sleep} from 'k6'


const port = 8080

const vus = 330

function getRandomNumber(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

export let options = {
    stages: [{duration: '10s', target: vus},
        {duration: '60s', target: vus},
        {duration: '10s', target: 0},
    ], thresholds: {
        http_req_duration: ['p(95)<1000'],
        http_req_failed: ['rate<0.01'],
    },
};


export default function () {
    const randomNum = getRandomNumber(1, vus);
    const response = http.get(`http://localhost:${port}/expand/${randomNum}`)
    check(response, {
        'status is 200': (r) => r.status === 200
    });
    sleep(1);
}
