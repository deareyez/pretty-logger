package com.deareyez.logger;

import org.apache.commons.lang3.ArrayUtils;

import android.util.Log;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 지원 타입.<br>
 * 1. primitive<br>
 * 2. collection<br>
 *
 * <br>
 * Version 0.4<br><br>
 * 추가 항목 : Object.class exception type 으로 추가..<br>
 * <br>
 * <br>
 * Version 0.3<br><br>
 * 추가 항목 : 더 이쁘게 더 아름답게 더 고져스하게 라인이 찍힘.<br>
 * 추가 항목 : 라인 우선순위 적용 primitive type, exception type 이 우선 그 외에는 밑으로 정렬.. .<br>
 * <br>
 * <br>
 * Version 0.2<br><br>
 * 추가 항목 : 최종 로그 대상은 primitive type 만 적용 하도록 수정..<br>
 * 추가 항목 : exception type byte 추가..<br>
 * <br>
 * <br>
 * Version 0.1<br><br>
 * 추가 항목 : prettyPrint 생성.<br>
 * <br>
 *
 * @author deareyez@gmail.com
 */
public class ReflectionLogger {

    /**
     * 간단한 부분에만.. 규모가 크면 퍼포먼스 책임 안짐..<br>
     * 빠를수도... 있고....<br><br><br><br><br>
     *
     * 문제점이 생기면 오대리인지 오사원인지 잘 모르겠지만<br>
     * 불러 주세요.<br>
     * @param obj logger 로 찍을 오브젝트..
     * @param tag logcat tag
     */
    public static void prettyPrint(Object obj, String tag) {
        String text = getToString(obj, 0);
        Log.d(tag, text);
    }

    /**
     * 간단한 부분에만.. 규모가 크면 퍼포먼스 책임 안짐..<br>
     * 빠를수도... 있고....<br><br><br><br><br>
     *
     * 문제점이 생기면 오대리인지 오사원인지 잘 모르겠지만<br>
     * 불러 주세요.<br>
     * @param obj logger 로 찍을 오브젝트..
     * @param tag logcat tag
     */
    @Deprecated
    public static void prettyPrintLong(Object obj, String tag) {

        String msg = getToString(obj, 0);
        if (msg != null && msg.length() > 4000) {

            for (int i = 0; i < msg.length() / 4000f; i++) {
                int start = i * 4000;
                int end = (i + 1) * 4000 > msg.length() ? msg.length() : (i + 1) * 4000;
                Log.d(tag, "" + msg.subSequence(start, end));
            }

        } else {
            Log.d(tag, msg);
        }
    }

    /**
     * 퍼포먼스 조심...
     */
    public static void prettyPrintln(Object obj, String tag) {

        String msg = getToString(obj, 0);

        for (String line : msg.split("\n")) {
            Log.d(tag, line);
        }
    }

    /**
     * 퍼포먼스 조심... 진짜루..!!!!
     */
    public static void prettyPrintLnThread(final Object obj, final String tag) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                String msg = getToString(obj, 0);

                for (String line : msg.split("\n")) {
                    Log.d(tag, line);
                }
            }
        }).start();
    }

    /**
     * 간단한 부분에만.. 규모가 크면 퍼포먼스 책임 안짐..
     */
    public static String getToString(Object obj, final int depth) {
        String value = getToString("root", obj, depth);
        return value;
    }

    private static String getToString(String tree, Object obj, final int depth) {
        if (obj == null || depth > 10) {
            return "";
        }

        Class<?> cls = obj.getClass();

        Field thisFieldList[] = cls.getDeclaredFields();
        Field[] superFieldList = cls.getSuperclass().getDeclaredFields();

        Field[] fieldList = ArrayUtils.addAll(superFieldList, thisFieldList);
        final int gap = 5;
        final int gapForDepth = depth * gap;
        final int lineLength = 150;
        String appendText;
        String gapText = "";
        String divideText = "";
        for (int i = 0; i < gapForDepth; i++) {
            if (i % gap == 0) {
                gapText += "|";
            } else {
                gapText += " ";
            }
        }
        for (int i = 0; i < lineLength - (gapForDepth); i++) {
            divideText += "-";
        }
        //Package fullPath 사용 여부는 글쎄..
        final String currentTree = tree + "-->" + cls.getSimpleName();
        String text = "\n" + gapText + divideText + "\n"
                + gapText + "|  " + cls.getSimpleName()
                + "\n" + gapText + divideText + "\n";

        // 제일 긴 필드명 찾기....
        // 제일 긴 필드 인스턴스 찾기...
        int fieldNameCnt = 0;

        for (Field field : fieldList) {
            field.setAccessible(true);
            String fieldName = field.getName();
            if (fieldName != null && fieldName.length() > fieldNameCnt) {
                fieldNameCnt = fieldName.length();
            }
        }

        List<String> textList = new ArrayList<String>();

        for (Field field : fieldList) {
            field.setAccessible(true);
            String fieldName = field.getName();
            String fieldValue = "";
            boolean isPrimitive = false;
            try {

                Object objInner = field.get(obj);

                if (objInner != null) {
                    fieldValue = objInner.toString();
                } else {
                    fieldValue = "";
                }

            } catch (IllegalAccessException | IllegalArgumentException e) {
                e.printStackTrace();
            }

            if (fieldValue == null) {
                fieldValue = "";
            }

            appendText = "";
            if (isExceptionType(field.getType())) {
                //Byte 만 있으니.. 일단 최상위..
                isPrimitive = true;
                appendText += getTextFormat(gapText, fieldNameCnt, fieldName, lineLength, "exception type[" + field.getType() + "]");
            } else if (field.getType().isPrimitive() || isWrapperType(field.getType())) {
                isPrimitive = true;
                appendText += getTextFormat(gapText, fieldNameCnt, fieldName, lineLength, fieldValue);
            } else if (field.getType().isArray()) {
                try {
                    if (field.get(obj) != null && Array.getLength(field.get(obj)) > 0) {
                        for (int i = 0; i < Array.getLength(field.get(obj)); i++) {
                            Object arrayObj = Array.get(field.get(obj), i);

                            if (arrayObj == null) {
                                appendText += getTextFormat(gapText, fieldNameCnt, fieldName, lineLength, "null");
                            } else if (arrayObj.getClass().isPrimitive() || isWrapperType(arrayObj.getClass())) {
                                appendText += getTextFormat(gapText, fieldNameCnt, fieldName, lineLength, arrayObj.toString());
                            } else {
                                appendText += getToString(currentTree, arrayObj, depth + 1);
                            }

                        }
                    } else {
                        appendText += getTextFormat(gapText, fieldNameCnt, fieldName, lineLength, "{ array length is zero }");
                    }
                } catch (ArrayIndexOutOfBoundsException
                        | IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else if (Collection.class.isAssignableFrom(field.getType())) {
                try {

                    Iterator<?> it = null;

                    if (field.get(obj) != null) {
                        it = ((Collection<?>) field.get(obj)).iterator();
                    }

                    if (it == null || it.hasNext() == false) {
                        appendText += getTextFormat(gapText, fieldNameCnt, fieldName, lineLength, "{ Collection length is zero }");
                    } else {
                        while (it.hasNext()) {
                            Object typeObj = it.next();
                            if (typeObj.getClass().isPrimitive() || isWrapperType(typeObj.getClass())) {
                                for (int i = 0; i < depth * gap; i++) {
                                    gapText += " ";
                                }
                                String format = "%-" + (fieldNameCnt + 1) + "s";
                                appendText += "\n" + gapText + "[   - " + String.format(format, fieldName) + "] = " + typeObj.toString();
                                appendText += getTextFormat(gapText, fieldNameCnt, fieldName, lineLength, typeObj.toString());
                            } else {
                                appendText += getToString(currentTree, typeObj, depth + 1);
                            }
                        }
                    }

                } catch (ArrayIndexOutOfBoundsException
                        | IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    //					Log.d("test","name : " + fieldName);
                    //					Log.d("test","value : " + fieldValue);
                    appendText += getToString(currentTree, field.get(obj), depth + 1);
                } catch (IllegalAccessException | IllegalArgumentException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
            if(isPrimitive) {
                textList.add(0, appendText);
            }
            else {
                textList.add(textList.size(), appendText);
            }
//            text += appendText;
        }

        for(String listTxt : textList) {
            text += listTxt;
        }

        text += "\n" + gapText + divideText + "\n";
        return text;
    }

    private static String getTextFormat(String gapText, int fieldNameCnt, String fieldName, int lineLength, String fieldValue) {
        String format;
        String convertText;
        if (gapText.length() > 0) {
            format = "\n%" + (gapText.length()) + "s" + "| %-" + (fieldNameCnt + 1) + "s | = " + fieldValue;
            convertText = String.format(format, gapText, fieldName, "|");
        } else {
            format = "\n| %-" + (fieldNameCnt + 1) + "s | = " + fieldValue;
            convertText = String.format(format, fieldName);
        }
//        if (lineLength - convertText.length() > 0) {
//            int cnt = lineLength - convertText.length();
//            for (int i = 0; i < cnt; i++) {
//                convertText += " ";
//            }
//            convertText += "|";
//        }
        return convertText;
    }

    public static boolean isWrapperType(Class<?> cls) {
        return cls.equals(Boolean.class) ||
                cls.equals(Integer.class) ||
                cls.equals(String.class) ||
                cls.equals(Character.class) ||
                cls.equals(Byte.class) ||
                cls.equals(Short.class) ||
                cls.equals(Double.class) ||
                cls.equals(Long.class) ||
                cls.equals(Float.class);
    }

    public static boolean isExceptionType(Class<?> cls) {
        return cls.equals(Byte.class)
                || Byte.TYPE.equals(cls)
                || Byte.TYPE.equals(cls.getComponentType())
                || cls.equals(Object.class);
    }
}
