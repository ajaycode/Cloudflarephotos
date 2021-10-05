#Cloudflare image management sample

## Getting Started

 This project contains sample code for the Cloudflare images API.

Code is available for:
1. Uploading an image
2. Checking if an image exists
3. Deleting an image on Cloudflare
4. Generation of a direct upload URL (for use in forms)

  This project uses Spring Boot, Thymeleaf and Cloudflare APIs

### Step 1 (Setup paid account with Cloudflare)
1. Create an account with Cloudflare. 

### Step 2 (Application configuration)

#### Cloudflare configuration in application.properties
```
Set the following values in application.properties, to try out this application:
These values are available from the Cloudflare portal (https://dash.cloudflare.com/<YOUR_ACCOUNT_ID>/images/variants)
cloudflare.account-id=YOUR_ACCOUNT_ID
cloudflare.image-delivery-url=https://imagedelivery.net/STRING_FROM_ABOVE_URL/

#[Get your API token here](https://dash.cloudflare.com/profile/api-tokens)
cloudflare.bearer-token=BEARER_TOKEN

cloudflare.image.direct-upload-request-url=https://api.cloudflare.com/client/v4/accounts/${cloudflare.account-id}/images/v1/direct_upload
cloudflare.image.delete-url.prefix=https://api.cloudflare.com/client/v4/accounts/${cloudflare.account-id}/images/v1/
cloudflare.image.upload-url=https://api.cloudflare.com/client/v4/accounts/${cloudflare.account-id}/images/v1
cloudflare.image.details-url.prefix=https://api.cloudflare.com/client/v4/accounts/${cloudflare.account-id}/images/v1/
```
#### Application configuration in application.properties
This is a directory on the application server, where the files will be uploaded to.  Files shall be uploaded from this folder onto Cloudflare.
Ensure that the account has read and write permissions to these folders.
```
application.images.directory-root=C:\\Temp\\Downloads
application.image-to-upload=C:\\Temp\\Downloads\\images.png
```

Place an image in the above-mentioned folder, to test for uploads.
```
private var pathToImageFile : String = "C:\\Temp\\image.jpg"
```
The above file is uploaded to Cloudflare directly using the ImageService service.  The existence of the image is verified and the image is deleted.
The code for this is available in Initializer class of CloudflarephotosApplication.kt

## Try it out
1. Build the project
2. Launch the application at [localhost](http://localhost:8080/)
3. Upload a file
   * The file is uploaded to the directory specified by application.images.directory-root
   * The file is then uploaded to Cloudflare
4. Navigate to https://dash.cloudflare.com/<YOUR_ACCOUNT_ID>/images/images to view the uploaded images

### Reference Documentation

For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.5.5/gradle-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.5.5/gradle-plugin/reference/html/#build-image)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/docs/2.5.5/reference/htmlsingle/#using-boot-devtools)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.5.5/reference/htmlsingle/#boot-features-developing-web-applications)
* [Thymeleaf](https://docs.spring.io/spring-boot/docs/2.5.5/reference/htmlsingle/#boot-features-spring-mvc-template-engines)

### Guides

The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/bookmarks/)
* [Handling Form Submission](https://spring.io/guides/gs/handling-form-submission/)

### Additional Links

These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)

