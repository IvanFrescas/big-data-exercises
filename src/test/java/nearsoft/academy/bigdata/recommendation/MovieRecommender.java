package nearsoft.academy.bigdata.recommendation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 *
 * @author ifrescas
 */

public class MovieRecommender {
    
    private int totalReviews = 0;
    private int totalProducts = 0;
    private int totalUsers = 0;

    //Hash map para almacenar ids
    //HashMap<String, Integer> productsMap = new HashMap<String, Integer>();
    //HashMap<String, Integer> usersMap = new HashMap<String, Integer>();

    BiMap<String, Integer> productsMap = HashBiMap.create();
    BiMap<String, Integer> usersMap = HashBiMap.create();


    public MovieRecommender(String path) throws  IOException {



        //Escritura de datos para crear archivo .csv
        File result = new File("Result.csv");
        FileWriter fw = new FileWriter(result);
        BufferedWriter bw = new BufferedWriter(fw);

        // Lectura de datos
        String infile = path;
        GZIPInputStream in = new GZIPInputStream(new FileInputStream(infile));
        Reader decoder = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(decoder);
        String line;

        String cvsProd="";
        String cvsUser="";
        String cvsScore = "";

        int prodNum = 0;
        int userNum = 0;
        int reviewNum = 0;


        while ((line = br.readLine()) != null) {
            if (line.startsWith("product/productId:")) {
                reviewNum++;

                String [] parts = line.split(": ");
                String productId = parts[1];
                
                if (!productsMap.containsKey(productId)) {
                    productsMap.put(productId,prodNum++);
                }
                cvsProd = productsMap.get(productId).toString();

            } else if (line.startsWith("review/userId")) {
                String [] parts = line.split(": ");
                String userId = parts[1];

                if(!usersMap.containsKey(userId)) {
                    usersMap.put(userId,userNum++);
                }
                cvsUser = usersMap.get(userId).toString();

            } 
            else if (line.startsWith("review/score")) {
                String [] parts = line.split(": ");
                cvsScore = parts[1];

                String entry = String.format("%s,%s,%s\n",   cvsUser ,cvsProd, cvsScore);
                bw.write(entry);
            }
        }
        
        br.close();
        bw.close();

        this.totalReviews = reviewNum;
        this.totalProducts = productsMap.size();
        this.totalUsers = usersMap.size();

    }

    public int getTotalReviews() {
        return this.totalReviews;
    }

    public int getTotalProducts() {
        return this.totalProducts;
    }

    public int getTotalUsers() {
        return this.totalUsers;
    }

    
    public List<String> getRecommendationsForUser(String userId) throws IOException, TasteException {

        List<String> results = new ArrayList<String>();

        int user = usersMap.get(userId);

        DataModel model = new FileDataModel(new File("Result.csv"));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

        List<RecommendedItem> recommendations = recommender.recommend(user, 3);

        for (RecommendedItem recommendation : recommendations) {
            results.add(productsMap.inverse().get((int)recommendation.getItemID()));
        }
        return results;

    }
}
