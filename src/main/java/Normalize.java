import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.omg.PortableInterceptor.INACTIVE;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Normalize {

    public static class NormalizeMapper extends Mapper<LongWritable, Text, Text, Text> {

        // map method
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

            //movieA:movieB \t relation
            //collect the relationship list for movieA

            // output: key: movieA; value: movieB=relation
            String line = value.toString().trim();
            String[] movie_movieRating = line.split("\t");
            String[] movieA_B = movie_movieRating[0].split(":");
            String outputKey = movieA_B[0];
            String outputValue = movieA_B[1] + "=" + movie_movieRating[1];
            context.write(new Text(outputKey), new Text(outputValue));
        }
    }

    public static class NormalizeReducer extends Reducer<Text, Text, Text, Text> {
        // reduce method
        @Override
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

            //key = movieA, value=<movieB:relation, movieC:relation...>
            //normalize each unit of co-occurrence matrix

            // we should transpose matrix here
            // output: key: movieB; value: movieA=relation/sum
            // attention: Iterable can be used just once, so we need to use other structure to store values
            int sum = 0;
            Map<String, Integer> map = new HashMap<String, Integer>();
            while (values.iterator().hasNext()) {
                String value = values.iterator().next().toString().trim();
                String[] movie_rating = value.split("=");
                String movieB = movie_rating[0];
                int rating = Integer.parseInt(movie_rating[1]);
                sum += rating;
                map.put(movieB, rating);
            }
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                String movieB = entry.getKey();
                int rating = entry.getValue();
                double relation = (double) rating / sum;
                String outputValue = key + "=" + String.valueOf(relation);
                context.write(new Text(movieB), new Text(outputValue));
            }
        }
    }

    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf);
        job.setMapperClass(NormalizeMapper.class);
        job.setReducerClass(NormalizeReducer.class);

        job.setJarByClass(Normalize.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        TextInputFormat.setInputPaths(job, new Path(args[0]));
        TextOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);
    }
}
