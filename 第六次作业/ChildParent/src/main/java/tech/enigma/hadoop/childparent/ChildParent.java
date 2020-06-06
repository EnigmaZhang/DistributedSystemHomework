package tech.enigma.hadoop.childparent;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ChildParent
{
    public static class Map extends Mapper<Object, Text, Text, Text>
    {
        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException
        {
            StringTokenizer token = new StringTokenizer(value.toString());

            while (token.hasMoreTokens())
            {
                String nextToken = token.nextToken();
                String[] attributes = nextToken.split(",");
                if (attributes.length == 2)
                {
                    // The child as key, father as value.
                    context.write(new Text(attributes[Family.CHILD.ordinal()]),
                            new Text("T" + attributes[Family.PARENT.ordinal()]));
                    context.write(new Text(attributes[Family.PARENT.ordinal()]),
                            new Text("F" + attributes[Family.CHILD.ordinal()]));
                }
                else
                {
                    System.err.println("Wrong data.");
                }
            }
        }
    }

    public static class Reduce extends Reducer<Text, Text, Text, Text>
    {
        @Override
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException
        {
            ArrayList<Text> children = new ArrayList<>();
            ArrayList<Text> parents = new ArrayList<>();
            for (Text value : values)
            {
                if (value.toString().charAt(0) == 'T')
                    parents.add(new Text(value.toString().substring(1)));
                else
                    children.add(new Text(value.toString().substring(1)));
            }
            for (Text child : children)
            {
                for (Text parent : parents)
                {
                    context.write(child, parent);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException
    {
        if (args.length != 2)
        {
            throw new IllegalArgumentException("Wrong numbers of arguments for main!");
        }

        Job job = Job.getInstance();
        job.setJobName("ClassCalculation");
        job.setJarByClass(ChildParent.class);
        job.setMapperClass(Map.class);
        job.setReducerClass(Reduce.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}

// The former is child, the latter is parent.
enum Family
{
    CHILD, PARENT
}

