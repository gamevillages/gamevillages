![image](https://github.com/minevillages/minevillages-user/assets/131671804/ef5b1464-044f-4588-841f-126a58ec89c6)
# Mine Villages User Service
This is User Service Server of Mine Villages Project made by Spring Framework. 

## Versions
- Java v21
- Spring Boot v3.2.4
- MySQL v8.0.34
- Redis v7.2.4

## Features
### Sign-up
- Email Verification
- Save Verify Number With Redis (TTL 5m)
- Argon2 Encryption
 
### Sign-in
- Save Session Key in Redis (TTL 1h)
- Send Email to user when illegal attempt is over 5 (Save count in Redis)
- Save Accessed Date And IP of Illegeal Attempt in Redis (Remove when login success)

### Log-out
- Remove Session Key from Redis
  
### Edit-Name
- Change Name
- Update updatedAt

### Sign-out
- Soft Delete (update deletedAt)
