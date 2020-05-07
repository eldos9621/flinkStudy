package com.my.study.flink.java;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.shaded.guava18.com.google.common.base.Throwables;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

/**
 * Created with Eldos.
 * Description:
 * Date: 2020-05-07
 */
public class StreamingWordCount {

    public static void main(String[] args) {
        try {
            //创建Flink 执行环境
            final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            //接受socket的输入流
            //使用本地900端口
            DataStream<String> text = env.socketTextStream("localhost", 9000, "\n");
            //使用flink算子对输入流的文本进行操作
            //按空格切词、计数、分组、设置时间窗口、聚合
            DataStream<Tuple2<String, Integer>> windowCounts = text.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
                @Override
                public void flatMap(String s, Collector<Tuple2<String, Integer>> collector) throws Exception {
                    for(String word : s.split("\\s")) {
                        collector.collect(Tuple2.of(word, 1));
                    }
                }
            })
                    .keyBy(0)
                    .timeWindow(Time.seconds(5))
                    .sum(1);
            //单线程打印结果
            windowCounts.print().setParallelism(1);
            env.execute("Socket Window WordCount");
        }catch (Exception e) {
            System.out.println(Throwables.getStackTraceAsString(e));
        }
    }

}
