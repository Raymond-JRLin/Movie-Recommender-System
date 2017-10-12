import org.apache.commons.collections.map.HashedMap;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.chain.ChainMapper;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Multiplication {
	public static class CooccurrenceMapper extends Mapper<LongWritable, Text, Text, Text> {

		// map method
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			//input: movieB \t movieA=relation

			//pass data to reducer
			String line = value.toString().trim();
			String[] movie_relation = line.split("\t");
			context.write(new Text(movie_relation[0]), new Text(movie_relation[1]));
		}
	}

	public static class RatingMapper extends Mapper<LongWritable, Text, Text, Text> {

		// map method
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

			//input: user,movie,rating
			//pass data to reducer
			//output: key: movieId; value: user:rating
			String line = value.toString().trim();
			String[] user_movie_rating = line.split(",");
			context.write(new Text(user_movie_rating[1]), new Text(user_movie_rating[0] + ":" + user_movie_rating[2]));
		}
	}

	public static class MultiplicationReducer extends Reducer<Text, Text, Text, DoubleWritable> {
		// reduce method
		@Override
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {

			//key = movieB value = <movieA=relation, movieC=relation... userA:rating, userB:rating...>
			//collect the data for each movie, then do the multiplication
			// output: key: user:movie; value: sub-rating
			Map<String, Double> movie_relation_map = new HashMap<String, Double>();
			Map<String, Double> user_rating_map = new HashMap<String, Double>();
			for (Text value :
					values) {
				String line = value.toString().trim();
				if (line.contains("=")) {
					String[] movie_relation = line.split("=");
					movie_relation_map.put(movie_relation[0], Double.parseDouble(movie_relation[1]));
				}
				if (line.contains(":")) {
					String[] user_rating = line.split(":");
					user_rating_map.put(user_rating[0], Double.parseDouble(user_rating[1]));
				}
			}
			for (Map.Entry<String, Double> relationEntry : movie_relation_map.entrySet()) {
				String movieId = relationEntry.getKey();
				Double relation = relationEntry.getValue();
				for (Map.Entry<String, Double> ratingEntry : user_rating_map.entrySet()) {
					String userId = ratingEntry.getKey();
					Double rating = ratingEntry.getValue();
					Double sub_rating = relation * rating;
					String outputKey = userId + ":" + movieId;
					context.write(new Text(outputKey), new DoubleWritable(sub_rating));
				}
			}
		}
	}


	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();

		Job job = Job.getInstance(conf);
		job.setJarByClass(Multiplication.class);

		ChainMapper.addMapper(job, CooccurrenceMapper.class, LongWritable.class, Text.class, Text.class, Text.class, conf);
		ChainMapper.addMapper(job, RatingMapper.class, Text.class, Text.class, Text.class, Text.class, conf);

		job.setMapperClass(CooccurrenceMapper.class);
		job.setMapperClass(RatingMapper.class);

		job.setReducerClass(MultiplicationReducer.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(DoubleWritable.class);

		MultipleInputs.addInputPath(job, new Path(args[0]), TextInputFormat.class, CooccurrenceMapper.class);
		MultipleInputs.addInputPath(job, new Path(args[1]), TextInputFormat.class, RatingMapper.class);

		TextOutputFormat.setOutputPath(job, new Path(args[2]));
		
		job.waitForCompletion(true);
	}
}
