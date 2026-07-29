package com.flowforge.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PythonScriptRunnerTest {

    private PythonScriptRunner runner;
    private List<String> logs;

    static boolean pythonAvailable() {
        try {
            Process process = new ProcessBuilder("python3", "--version").start();
            return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
        } catch (Exception ex) {
            return false;
        }
    }

    @BeforeEach
    void setUp() {
        runner = new PythonScriptRunner();
        setField(runner, "pythonExecutable", "python3");
        setField(runner, "timeoutSeconds", 30L);
        setField(runner, "maxScriptChars", 100000);
        setField(runner, "maxOutputChars", 65536);
        logs = new ArrayList<>();
    }

    @Test
    void rejectsEmptyScript() {
        assertThatThrownBy(() -> runner.run("  ", logs::add))
                .isInstanceOf(PythonScriptRunner.PythonExecutionException.class)
                .hasMessageContaining("empty");
    }

    @Test
    @EnabledIf("pythonAvailable")
    void runsSimpleScript() throws Exception {
        PythonScriptRunner.PythonRunResult result = runner.run(
                "print('hello from flowforge')",
                logs::add);

        assertThat(result.exitCode()).isZero();
        assertThat(result.stdout()).contains("hello from flowforge");
        assertThat(logs).anyMatch(line -> line.contains("stdout"));
    }

    @Test
    @EnabledIf("pythonAvailable")
    void failsOnNonZeroExit() {
        assertThatThrownBy(() -> runner.run("import sys\nsys.exit(1)", logs::add))
                .isInstanceOf(PythonScriptRunner.PythonExecutionException.class)
                .hasMessageContaining("exit code 1");
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
}
