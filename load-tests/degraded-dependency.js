import http from 'k6/http'; import {check,sleep} from 'k6'; const base=__ENV.BASE_URL||'http://localhost:18080';
export const options={vus:Number(__ENV.VUS||3),duration:__ENV.DURATION||'90s'};
export default function(){const r=http.post(`${base}/api/orders`,JSON.stringify({item:'slow-payment',amount:42}),{headers:{'Content-Type':'application/json'}});check(r,{'order persisted':x=>x.status===201});sleep(.3);}
