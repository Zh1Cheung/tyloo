package io.tyloo.api.Enums;
import io.tyloo.core.exception.TylooRuntimeException;
import lombok.Getter;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum EventType {

    /*
     * ��������
     */
    SAVE(0, "����"),

    /*
     * ɾ������
     */
    DELETE(1, "ɾ��"),

    /*
     * ��������
     */
    UPDATE(2, "����״̬");

    private final int code;

    private final String desc;

    /**
     * Build by code event type enum.
     *
     * @param code the code
     * @return the event type enum
     */
    public static EventType buildByCode(int code) {
        return Arrays.stream(EventType.values()).filter(e -> e.code == code).findFirst()
                .orElseThrow(() -> new TylooRuntimeException("can not support this code!"));
    }

}
