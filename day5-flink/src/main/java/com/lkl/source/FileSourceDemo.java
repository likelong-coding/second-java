package com.lkl.source;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.file.src.reader.TextLineInputFormat;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.File;

/**
 * @author likelong
 * @date 2023/11/4 23:37
 * @description
 */
public class FileSourceDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        FileSource<String> fileSource = FileSource.forRecordStreamFormat(
                new TextLineInputFormat(),
                Path.fromLocalFile(new File("D:\\workspace\\IdeaProjects\\second-java\\day5-flink\\src\\main\\resources\\input\\words.txt"))

        ).build();
        env.
                fromSource(fileSource, WatermarkStrategy.noWatermarks(), "fileSource")
                .print();

        env.execute();
    }
}
