package una.proyecto.utils;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.LocalTime;

public class LocalTimeAdapter extends XmlAdapter<String, LocalTime> {
    @Override
    public LocalTime unmarshal(String value) throws Exception {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalTime.parse(value);
    }

    @Override
    public String marshal(LocalTime value) throws Exception {
        if (value == null) {
            return null;
        }
        return value.toString();
    }
}
