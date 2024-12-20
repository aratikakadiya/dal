package ca.nrc.cadc.dali.tables.parquet;

import ca.nrc.cadc.dali.tables.votable.VOTableField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.log4j.Logger;

public class DynamicSchemaGenerator {

    private static final Logger log = Logger.getLogger(DynamicSchemaGenerator.class);

    public static Schema generateSchema(List<VOTableField> voFields) {
        // List to hold Avro fields
        List<Schema.Field> fields = new ArrayList<>();
        try {
            int columnCount = voFields.size();
            log.debug("VOTable Column count = " + columnCount);
            for (VOTableField voField : voFields) {
                String columnName = voField.getName();
                Schema.Field field = new Schema.Field(columnName.replaceAll("\"", ""), getAvroFieldType(voField.getDatatype()), null, null);
                fields.add(field);
            }
            log.debug("Avro Schema.Field count = " + fields.size());
        } catch (Exception e) {
            log.debug("Failure while creating Avro Schema from VOTable", e);
            throw new RuntimeException("Failure while creating Avro Schema from VOTable : " + e.getMessage(), e);
        }

        // Define the Avro record schema with the fields
        Schema schema = Schema.createRecord("Record", null, null, Boolean.FALSE);
        schema.setFields(fields);
        log.debug("Schema Generated Successfully : " + schema);
        return schema;
    }

    private static Schema getAvroFieldType(String sqlType) {
        // Map SQL data types to Avro data types
        Schema fieldType;
        switch (sqlType) {
            case "int":
                fieldType = Schema.create(Schema.Type.INT);
                break;
            case "long":
                fieldType = Schema.create(Schema.Type.LONG);
                break;
            case "float":
                fieldType = Schema.create(Schema.Type.FLOAT);
                break;
            case "double":
                fieldType = Schema.create(Schema.Type.DOUBLE);
                break;
            case "char":
                fieldType = Schema.create(Schema.Type.STRING);
                break;
            case "boolean":
                fieldType = Schema.create(Schema.Type.BOOLEAN);
                break;
            case "date":
            case "timestamp":
                fieldType = Schema.create(Schema.Type.STRING); // TODO: TBD
                break;
            default:
                fieldType = Schema.create(Schema.Type.STRING);
        }
        return Schema.createUnion(Arrays.asList(Schema.create(Schema.Type.NULL), fieldType)); // TODO: TBD
    }
}

