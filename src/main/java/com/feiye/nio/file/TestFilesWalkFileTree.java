package com.feiye.nio.file;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

public class TestFilesWalkFileTree {
    public static void main(String[] args) throws IOException {
        // 要遍历的根目录
        Path path = Paths.get("D:\\PersonPrograms\\jdk1.8.0_202");
//        runStatistics(path);
//        runJarStatistics(path);
        runDelete();
//        runCopy();
    }

    public static void runJarStatistics(Path path) throws IOException {
        AtomicInteger fileCount = new AtomicInteger();

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".jar")) {
                    //System.out.println(file);
                    fileCount.incrementAndGet();
                }
                return super.visitFile(file, attrs);
            }
        });

        // 输出 .jar 文件总数
        System.out.println("JAR 文件数：" + fileCount);
    }

    public static void runStatistics(Path path) throws IOException {
        // 记录目录数量
        AtomicInteger dirCount = new AtomicInteger();

        // 记录文件数量
        AtomicInteger fileCount = new AtomicInteger();

        // 遍历目录和文件
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // 打印目录
                System.out.println(dir);

                // 统计目录数量
                dirCount.incrementAndGet();

                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // 打印文件
                System.out.println(file);

                // 统计文件数量
                fileCount.incrementAndGet();

                return super.visitFile(file, attrs);
            }
        });

        // 输出目录总数
        System.out.println("目录数：" + dirCount);

        // 输出文件总数
        System.out.println("文件数：" + fileCount);
    }

    /**
     * Files.walkFileTree() 来递归删除目录及其内容
     * 注意删除操作的风险：删除是一个非常危险的操作，特别是递归删除目录时，务必要确保该目录中的文件没有重要数据。
     */
    public static void runDelete() throws IOException {
        Path path = Paths.get("D:\\PersonPrograms\\Haozip_copy");
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // 删除文件
                Files.delete(file);
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                // 需要先删除文件，然后目录：删除空目录
                Files.delete(dir);
                return super.postVisitDirectory(dir, exc);
            }
        });
    }

    public static void runCopy() throws IOException {
        long start = System.currentTimeMillis();

        // 源目录
        String source = "D:\\PersonPrograms\\Haozip";
        // 目标目录
        String target = "D:\\PersonPrograms\\Haozip_copy";

        /*Files.walkFileTree(Paths.get(source), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // 创建目标目录
                String targetName = dir.toString().replace(source, target);
                Files.createDirectory(Paths.get(targetName));
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // 拷贝文件
                String targetName = file.toString().replace(source, target);
                Files.copy(file, Paths.get(targetName));
                return super.visitFile(file, attrs);
            }
        });*/

        // 遍历源目录
        Files.walk(Paths.get(source)).forEach(path -> {
            try {
                // 将源路径转换为目标路径
                String targetName = path.toString().replace(source, target);

                // 如果是目录
                if (Files.isDirectory(path)) {
                    // 创建目录
                    Files.createDirectory(Paths.get(targetName));
                    // 如果是文件
                } else if (Files.isRegularFile(path)) {
                    // 拷贝文件
                    Files.copy(path, Paths.get(targetName));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
