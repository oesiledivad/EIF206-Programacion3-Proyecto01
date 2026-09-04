package una.proyecto.utils;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.LocalDate;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {
    @Override
    public LocalDate unmarshal(String value) throws Exception {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }

    @Override
    public String marshal(LocalDate value) throws Exception {
        if (value == null) {
            return null;
        }
        return value.toString();
    }
}
