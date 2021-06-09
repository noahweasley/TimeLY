package com.tools.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class VectorDrawableToSVG {
    private static final String filePath
            = "C:\\Users\\Noah\\StudioProjects\\TimeLY\\app\\src\\main\\res\\drawable\\ic_alarm_clock.xml";
    private static final String storagePath = "C:\\Users\\Noah\\Desktop\\ic_alarm_clock.svg";

    public static void main(String[] args) {
        startConversion();
    }

    private static void startConversion() {
        System.out.println("Script created by Noah");
        final File target = new File(filePath);

        if (target.exists()) {
            if (target.isFile()) {
                System.out.println("Starting VectorDrawableToSVG");

                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(storagePath);
                    FileInputStream fileInputStream = new FileInputStream(target);

                    int data;
                    StringBuilder dataBuilder = new StringBuilder();
                    while ((data = fileInputStream.read()) != -1) {
                        // copy to specified directory
                        dataBuilder.append((char) data);
                    }

                    String dataString = dataBuilder.toString();
                    //noinspection ResultOfMethodCallIgnored
                    String head1 = "<vector xmlns:android=\"http://schemas" +
                            ".android.com/apk/res/android\"";
                    String head2 = "<svg xmlns=\"http://www.w3.org/2000/svg\"";

                    String result = dataString.replaceFirst(head1, head2)
                                              .replaceFirst("android:width", "width")
                                              .replaceFirst("android:height", "height")
                                              .replaceAll("dp", "")
                                              .replaceAll("android:pathData", "d")
                                              .replaceAll("android:fillColor", "fill")
                                              .replaceAll("android:strokeColor", "stroke")
                                              .replaceAll("android:strokeAlpha", "stroke-opacity")
                                              .replaceAll("android:fillAlpha", "fill-opacity")
                                              .replaceAll("android:strokeWidth",
                                                          "stroke-width")
                                              .replaceFirst("android:viewportWidth=\"[\\d]+\"", "")
                                              .replaceFirst("android:viewportHeight=\"[\\d]+\"", "")
                                              .replaceAll("android:fillType", "fill-rule");
                    for (int i = 0; i < result.length(); i++) {
                        fileOutputStream.write(result.charAt(i));
                    }
                } catch (IOException e) {
                    System.out.println("Error occurred: " + e.getMessage());
                }

                System.out.println("Conversion successful");

            } else {
                System.out.println("No operations yet");
            }
        } else {
            System.out.println(filePath + " does not exist");
        }
    }

}
