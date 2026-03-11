# High-Level Design: Email Application - Back-of-the-Envelope (BoE) Estimation (Step 5)

## Overview
Back-of-the-Envelope (BoE) estimation provides quick approximations for system scale, focusing on storage and bandwidth for an email application handling global users. This simplified version is tailored for interviews, using order-of-magnitude calculations with reasonable assumptions. It emphasizes core metrics: daily emails, storage needs, and network traffic (reads/writes).

BoE principles:
- Round numbers for simplicity.
- Conservative estimates; consider peaks.
- Validate against real-world (e.g., Gmail: ~1.8B users, billions of emails/day).

## 1. Assumptions
Start with reasonable assumptions.

**Users**
- Total users = 1B
- Daily active users (DAU) ≈ 20% → 200M active users/day

**Email Activity**
- Average emails sent per active user per day = 10
- Average email size (including attachments, metadata, headers) = 75 KB

So:
- Emails/day = 200M × 10 = 2B emails/day

## 2. Storage Requirement Per Day
- 2B emails × 75 KB = 150B KB = 150 TB/day

So the system stores about 150 TB/day of new email data.

**Explanation**: Multiply daily emails by average size. For annual storage (1-year retention): ~55 PB (365 × 150 TB), but focus on daily for BoE. Use sharding/S3 for scale.

## 3. Bandwidth Requirement (Write Traffic)
Emails are written once but read multiple times.

**Write Bandwidth**:
- 150 TB/day
- Convert to per second: 150 TB / 86,400 sec ≈ 1.78 GB/s

So write traffic ≈ 1.8 GB/s

**Explanation**: Daily storage / seconds in a day. Writes include incoming API payloads and DB inserts.

## 4. Read Traffic (Much Larger)
Emails are read multiple times:
- User opens email
- Mobile sync
- Web client refresh
- Search indexing

Assume:
- Each email is read 3 times on average

**Read Traffic** = 3 × write traffic ≈ 450 TB/day
- Per second: 450 TB/day ≈ 5.3 GB/s

**Explanation**: Reads dominate (80/20 read/write ratio). Multiply writes by read factor for total access.

## 5. Total Bandwidth
- Writes = 1.8 GB/s
- Reads = 5.3 GB/s
- **Total** ≈ 7 GB/s

Convert to bits:
- 7 GB/s × 8 = 56 Gbps

**Explanation**: Sum read + write. Use CDN for attachments to offload.

## 6. Peak Traffic
Internet traffic is bursty.

- Typical peak = 2–3× average.
- Peak ≈ 7 GB/s × 3 ≈ 21 GB/s

Convert to network bandwidth:
- 21 GB/s × 8 ≈ 168 Gbps

**Explanation**: Multiply average by peak factor (e.g., business hours). Provision 200 Gbps links for headroom.

## 7. Final BoE Summary

| Metric                  | Value          |
|-------------------------|----------------|
| Total users             | 1B             |
| Daily active users      | 200M           |
| Emails/day              | 2B             |
| Storage/day             | 150 TB         |
| Write bandwidth         | 1.8 GB/s       |
| Read bandwidth          | 5.3 GB/s       |
| Average total bandwidth | ~7 GB/s        |
| Peak bandwidth          | ~20 GB/s (~160 Gbps) |

**Explanation**: This summary captures essentials. For interviews, explain assumptions first, then derive metrics step-by-step. Adjust for specifics (e.g., higher reads = more bandwidth).

## Potential Optimizations
- Caching (Redis): Reduce reads by 50-80%.
- Compression: Gzip saves 30%.
- Sharding: Distribute load across regions.
- Monitoring: Alert at 70% capacity.

