package es.codeurjc.mca.practica_1_cloud_ordinaria_2021.image;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

@Service("storageService")
@Profile("production")
public class S3ImageService implements ImageService {

    private final Logger logger = LoggerFactory.getLogger(S3ImageService.class);
    private AmazonS3Client s3;

    private String bucketName;

    public S3ImageService(@Value("${amazon.s3.region}") String region,
                          @Value("${amazon.s3.bucket-name}") String bucketName) {
        this.s3 = (AmazonS3Client) AmazonS3ClientBuilder
                .standard()
                .withRegion(region)
                .build();
        this.bucketName = bucketName;
        createBucket();
    }

    private void createBucket() {
        if(s3.doesBucketExistV2(this.bucketName)) {
            logger.info("{} already exists.", this.bucketName);
            return;
        }

        CreateBucketRequest cbr = new CreateBucketRequest(this.bucketName);
        cbr.setCannedAcl(CannedAccessControlList.PublicReadWrite);

        AccessControlList acl = new AccessControlList();
        // Create a collection of grants to add to the bucket.
        ArrayList<Grant> grantCollection = new ArrayList<Grant>();

        // Grant the account owner full control.
        Grant grant1 = new Grant(new CanonicalGrantee(s3.getS3AccountOwner().getId()), Permission.FullControl);
        grantCollection.add(grant1);

        // Grant the account owner full control.
        Grant grant2 = new Grant(GroupGrantee.AllUsers, Permission.Read);
        grantCollection.add(grant2);

        // Grant the account owner full control.
        Grant grant3 = new Grant(GroupGrantee.AllUsers, Permission.ReadAcp);
        grantCollection.add(grant3);

        acl.grantAllPermissions(grantCollection.toArray(new Grant[0]));
        cbr.setAccessControlList(acl);
        s3.createBucket(cbr);
        logger.info("New bucket created: {}", this.bucketName);
    }

    @Override
    public String createImage(MultipartFile multiPartFile) {
        try {
            String privateObjectName = "image_" + UUID.randomUUID() + "_" +multiPartFile.getOriginalFilename();
            String path = "events/" + privateObjectName;
            logger.info("Upload object: "+privateObjectName+" to bucket: " + this.bucketName);
            String fileName = multiPartFile.getOriginalFilename();
            File file = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
            multiPartFile.transferTo(file);
            PutObjectRequest por = new PutObjectRequest(
                    this.bucketName,
                    path,
                    file
            );
            por.setCannedAcl(CannedAccessControlList.PublicRead);
            s3.putObject(por);
            return s3.getResourceUrl(this.bucketName, path);
        } catch (Exception e) {
            logger.error("Error:", e);
        }
        return "Error uploading image";
    }

    @Override
    public void deleteImage(String image) {
        String imageId = image.substring(image.indexOf("events"));
        logger.info("Image to delete: " + imageId);
        s3.deleteObject(bucketName, imageId);
    }
}
