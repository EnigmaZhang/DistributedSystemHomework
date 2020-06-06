package tech.enigma.hadoop.scorecalculation;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.StringTokenizer;

public class ScoreCalculation
{
    public static class Map extends Mapper<Object, Text, Text, IntWritable>
    {
        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException
        {
            StringTokenizer token = new StringTokenizer(value.toString());

            while (token.hasMoreTokens())
            {
                String nextToken = token.nextToken();
                String[] attributes = nextToken.split(",");
                if (attributes.length == 5)
                {
                    if (attributes[3].equals("必修"))
                    {
                        // The name as key, score as value.
                        context.write(new Text(attributes[StudentAttr.NAME.ordinal()]),
                                new IntWritable(Integer.parseInt(attributes[StudentAttr.SCORE.ordinal()])));
                    }
                }
                else
                {
                    System.err.println("Wrong data.");
                }
            }
        }
    }

    public static class Reduce extends Reducer<Text, IntWritable, Text, DoubleWritable>
    {
        @Override
        public void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException
        {
            int sum = 0, count = 0;

            for (IntWritable value : values)
            {
                sum += value.get();
                count++;
            }
            context.write(key, new DoubleWritable(sum * 1.0 / count));
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException
    {
        if (args.length != 2)
        {
            throw new IllegalArgumentException("Wrong numbers of arguments for main!");
        }

        Job job = Job.getInstance();
        job.setJobName("ScoreCalculation");
        job.setJarByClass(ScoreCalculation.class);
        job.setMapperClass(Map.class);
        job.setReducerClass(Reduce.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}

enum StudentAttr
{
    CLASS, NAME, COURSE, PROPERTY, SCORE;
}
