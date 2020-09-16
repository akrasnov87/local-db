package ru.mobnius.localdb.data.tablePack;

public class CsvUtil {
    public static Table convert(String input) {
        if(input != null && !input.equals("")) {
            String[] lines = input.split("\n");
            if(lines.length > 0) {
                String[] columns = lines[0].split("\\|");
                Table table = new Table(columns, lines.length - 1);

                for(int i = 1; i < lines.length; i++) {
                    String line = lines[i];
                    String[] values = new String[columns.length];
                    int idx = 0;
                    for (String s : line.split("\\|")) {
                        values[idx] = s;
                        idx++;
                    }

                    table.addValues(values);
                }

                return table;
            }
        }
        return null;
    }
}
