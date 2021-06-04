package work.lclpnet.launcherlogic.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class LoggingPrintStream extends PrintStream {

    public static PrintStream origErr = null;
    protected OutputStream loggingStream;

    public LoggingPrintStream(OutputStream out, OutputStream loggingStream) {
        super(out, true);
        this.loggingStream = loggingStream;
    }

    protected void newLine() {
        try {
            loggingStream.write(new byte[]{10});
        } catch (IOException e) {
            if (origErr != null) e.printStackTrace(origErr);
        }
    }

    private void writeString(String x) {
        try {
            loggingStream.write(x.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            if (origErr != null) e.printStackTrace(origErr);
        }
    }

    private void writeBoolean(boolean b) {
        writeString(String.valueOf(b));
    }

    private void writeChar(char c) {
        writeString(String.valueOf(c));
    }

    private void writeCharArray(char[] c) {
        writeString(String.valueOf(c));
    }

    private void writeDouble(double d) {
        writeString(String.valueOf(d));
    }

    private void writeFloat(float f) {
        writeString(String.valueOf(f));
    }

    private void writeInt(int i) {
        writeString(String.valueOf(i));
    }

    private void writeLong(long l) {
        writeString(String.valueOf(l));
    }

    private void writeObj(Object o) {
        writeString(o.toString());
    }

    @Override
    public void println() {
        super.println();
        newLine();
    }

    @Override
    public void print(String s) {
        super.print(s);
        writeString(s);
    }

    @Override
    public void println(String x) {
        super.println(x);
        newLine();
    }

    @Override
    public void print(boolean b) {
        super.print(b);
        writeBoolean(b);
    }

    @Override
    public void println(boolean x) {
        super.println(x);
        newLine();
    }

    @Override
    public void print(char c) {
        super.print(c);
        writeChar(c);
    }

    @Override
    public void println(char x) {
        super.println(x);
        newLine();
    }

    @Override
    public void print(char[] s) {
        super.print(s);
        writeCharArray(s);
    }

    @Override
    public void println(char[] x) {
        super.println(x);
        newLine();
    }

    @Override
    public void print(double d) {
        super.print(d);
        writeDouble(d);
    }

    @Override
    public void println(double x) {
        super.println(x);
        newLine();
    }

    @Override
    public void print(float f) {
        super.print(f);
        writeFloat(f);
    }

    @Override
    public void println(float x) {
        super.println(x);
        newLine();
    }

    @Override
    public void print(int i) {
        super.print(i);
        writeInt(i);
    }

    @Override
    public void println(int x) {
        super.println(x);
        newLine();
    }

    @Override
    public void print(long l) {
        super.print(l);
        writeLong(l);
    }

    @Override
    public void println(long x) {
        super.println(x);
        newLine();
    }

    @Override
    public void print(Object obj) {
        super.print(obj);
        writeObj(obj);
    }

    @Override
    public void println(Object x) {
        super.println(x);
        newLine();
    }

    @Override
    public PrintStream printf(Locale l, String format, Object... args) {
        writeString(String.format(l, format, args));
        return super.printf(l, format, args);
    }

}
