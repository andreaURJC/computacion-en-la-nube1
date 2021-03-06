package es.codeurjc.mca.practica_1_cloud_ordinaria_2021.image;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
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
@Profile("dev")
public class S3ImageService implements ImageService {

    private final Logger logger = LoggerFactory.getLogger(S3ImageService.class);
    private AmazonS3Client s3;

    @Value("${amazon.s3.bucket-name}")
    private String bucketName;

    @Value("${amazon.s3.endpoint}")
    private String bucketEndpoint;

    public S3ImageService(@Value("${amazon.s3.region}") String region) {
        this.s3 = (AmazonS3Client) AmazonS3ClientBuilder
                .standard()
                .withRegion(region)
                .build();
    }

    @Override
    public String createImage(MultipartFile multiPartFile) {
        String privateObjectName = "image_" + UUID.randomUUID() + "_" +multiPartFile.getOriginalFilename();
        String path = "events/"+ privateObjectName;
        logger.info("Upload object: "+privateObjectName+" to bucket: "+bucketName);

        PutObjectRequest por = new PutObjectRequest(
                bucketName,
                privateObjectName,
                new File(path)
        );
        por.setCannedAcl(CannedAccessControlList.PublicRead);
        s3.putObject(por);
        return s3.getResourceUrl(bucketName, path);
    }

    @Override
    public void deleteImage(String image) {

    }
}
