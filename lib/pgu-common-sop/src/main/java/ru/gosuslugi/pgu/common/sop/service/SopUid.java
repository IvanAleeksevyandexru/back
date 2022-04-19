package ru.gosuslugi.pgu.common.sop.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum SopUid {
    PFR_DIVISIONS(
            "42fd59f8-cea9-41f8-9fad-f53c74aec567",
            Map.of(
                    "divisionCode","1a4fc9f7-1014-4376-a3ec-b96056bdcf3d",
                    "toPfrCode","8caf6cb5-43a0-46b1-bf57-2bb1462a10c0",
                    "name","b8bc7170-8635-42d0-8ec3-604e3ee3dd2c",
                    "shortName","c31f0a0e-79b4-4af3-9c66-9fc7bd830b00",
                    "type","c247f83d-55a6-46b7-91b2-6b41c41e5ca1",
                    "OKATO","687df7d4-7ec6-470c-9a1e-135fc7c5ab30",
                    "OKTMO","99cfc5ba-e386-4ad5-b6a2-6543c9a80c38",
                    "isJuristical","a1b4db31-10ae-4864-8301-f7a66bca102f"
            )
    ),

    PFR_DIVISIONS_AREAS(
            "b7a3b320-727f-46b4-8365-dab559e32697",
            Map.of(
                    "divisionCode", "bbe8a32f-75f7-4f75-894d-5dcc0df178ea",
                    "areaOkato", "e11f6f68-6140-425a-837d-10c44fd8bf4a",
                    "areaOktmo", "3669c3b7-2c05-4a04-9df8-0eac86c0692a",
                    "id", "95e4a173-a253-421a-ae0c-6499f266dc86"
            )
    ),
    UNKNOWN("", Collections.emptyMap());

    /** Маппер по uid справочника. */
    private static final Map<String, SopUid> DICT_NAME_TO_UID_MAPPER = Arrays.stream(SopUid.values())
            .collect(Collectors.toUnmodifiableMap(SopUid::getSopSourceUid, Function.identity()));

    /** Идентификатор справочника в СОП. */
    private final String sopSourceUid;
    /** Map из имён колонок и их uid в СОП. */
    private final Map<String, String> columnMap;

    SopUid(String sopSourceUid, Map<String, String> columnMap) {
        this.sopSourceUid = sopSourceUid;
        this.columnMap = columnMap;
    }

    public String getSopSourceUid() {
        return sopSourceUid;
    }

    public String getColumnUid(String columnName) {
        return columnMap.get(columnName);
    }

    public Collection<String> getColumnUids() {
        return columnMap.values();
    }

    public Collection<String> getColumnUids(Set<String> columnNames) {
        return columnMap.entrySet().stream()
                .filter(entry -> columnNames.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toUnmodifiableSet());
    }

    public static SopUid getSopElementByUid(String sopSourceUid) {
        return DICT_NAME_TO_UID_MAPPER.getOrDefault(sopSourceUid, UNKNOWN);
    }
}
