# Railway Deployment Guide

## Prerequisites
- Railway account
- MySQL database service running on Railway
- Application deployed on Railway

## Database Configuration

### Railway Environment Variables

Set these in your Railway project's Variables section:

```
DATABASE_URL=jdbc:mysql://mysql.railway.internal:3306/railway?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=ojXlriTrltsiJcylbZTFdrBXrvKwjWaW
PORT=8080
SPRING_PROFILES_ACTIVE=railway
```

### Railway MySQL Connection Details
- **Host**: mysql.railway.internal
- **Port**: 3306
- **Database**: railway
- **Username**: root
- **Password**: ojXlriTrltsiJcylbZTFdrBXrvKwjWaW

## Deployment Steps

1. **Build the application**:
   ```bash
   ./mvnw clean package -DskipTests
   ```

2. **Deploy to Railway**:
   - Railway will automatically detect the Spring Boot application
   - Set the environment variables in Railway dashboard
   - Railway will expose the application on a public URL

3. **Health Check**:
   ```bash
   curl https://your-app.railway.app/api/products
   ```

## Railway Service Configuration

Railway will automatically:
- Detect Java/Maven project
- Install Java 17
- Run `mvn clean install`
- Execute the generated JAR file
- Expose the service on HTTPS

## Troubleshooting

### Connection Issues
- Verify MySQL service is running on Railway
- Check environment variables are set correctly
- Ensure firewall rules allow connection
- Verify MYSQL_PUBLIC_URL matches the configuration

### Logs
View logs in Railway dashboard to debug connection issues:
```
railway logs
```
