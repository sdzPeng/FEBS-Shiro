package cc.mrbird.febs.common.configure;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
 
/**
 * mongodb的配置类
 * 解决新版本不支持获取GGridFSDBFile
 */
@Configuration
public class MongoConf {
 
    @Autowired
    private MongoDbFactory mongoDbFactory;
 
    @Bean
    public GridFSBucket getGridFSBucket() {
        MongoDatabase db = mongoDbFactory.getDb();
        return GridFSBuckets.create(db);
    }
}