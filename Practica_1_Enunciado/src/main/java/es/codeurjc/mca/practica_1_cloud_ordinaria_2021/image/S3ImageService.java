package es.codeurjc.mca.practica_1_cloud_ordinaria_2021.image;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@Service("storageService")
@Profile("production")
public class S3ImageService implements ImageService {

    private final Logger logger = LoggerFactory.getLogger(S3ImageService.class);
    private AmazonS3Client s3;

    private String bucketName;

    @Value("${amazon.s3.endpoint}")
    private String bucketEndpoint;

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
        s3.createBucket(this.bucketName);
        logger.info("New bucket created: {}", this.bucketName);
    }

    @Override
    public String createImage(MultipartFile multiPartFile) {
        try {
            String privateObjectName = "image_" + UUID.randomUUID() + "_" +multiPartFile.getOriginalFilename();
            String path = "events/" + privateObjectName;
            logger.info("Upload object: "+privateObjectName+" to bucket: " + this.bucketName);
            String fileName = multiPartFile.getOriginalFilename();
            File file = new File(System.getProperty("java.io.tmpdir") + fileName);
            multiPartFile.transferTo(file);
            PutObjectRequest por = new PutObjectRequest(
                    bucketName,
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
