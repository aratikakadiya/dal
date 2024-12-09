package ca.nrc.cadc.dali.tables.parquet;

import ca.nrc.cadc.dali.tables.TableWriter;
import ca.nrc.cadc.dali.tables.votable.VOTableDocument;
import ca.nrc.cadc.dali.tables.votable.VOTableResource;
import ca.nrc.cadc.dali.util.FormatFactory;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.io.OutputFile;
import org.apache.parquet.io.PositionOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

public class ParquetWriter implements TableWriter<VOTableDocument> {

    private static final Logger log = Logger.getLogger(ParquetWriter.class);

    private FormatFactory formatFactory;

    public ParquetWriter() {

    }

    @Override
    public String getExtension() {
        return "parquet";
    }

    @Override
    public String getContentType() {
        return "application/vnd.apache.parquet";
    }

    @Override
    public String getErrorContentType() {
        return "text/plain";
    }

    @Override
    public void setFormatFactory(FormatFactory ff) {
        this.formatFactory = ff;
    }

    @Override
    public void write(VOTableDocument tm, OutputStream out) throws IOException {
        write(tm, out, Long.MAX_VALUE);
    }

    @Override
    public void write(VOTableDocument resultSet, OutputStream out, Long maxRec) throws IOException {
        log.debug("ParquetWriter Write service called. MaxRec = " + maxRec);
        for (VOTableResource resource : resultSet.getResources()) {
            Schema schema = DynamicSchemaGenerator.generateSchema(resource.getTable().getFields());
            OutputFile outputFile = outputFileFromStream(out);

            try (org.apache.parquet.hadoop.ParquetWriter<GenericRecord> writer = AvroParquetWriter.<GenericRecord>builder(outputFile)
                    .withSchema(schema)
                    .withCompressionCodec(CompressionCodecName.SNAPPY)
                    .withRowGroupSize(Long.valueOf(org.apache.parquet.hadoop.ParquetWriter.DEFAULT_BLOCK_SIZE))
                    .withPageSize(org.apache.parquet.hadoop.ParquetWriter.DEFAULT_PAGE_SIZE)
                    .withConf(new Configuration())
                    .withWriteMode(ParquetFileWriter.Mode.OVERWRITE)
                    .withValidation(false)
                    .withDictionaryEncoding(false)
                    .build()) {

                Iterator<List<Object>> iterator = resource.getTable().getTableData().iterator();
                int recordCount = 1;

                while (iterator.hasNext() && recordCount <= maxRec) {
                    GenericRecord record = new GenericData.Record(schema);
                    List<Object> rowData = iterator.next();
                    int columnIndex = 0;

                    for (Schema.Field field : schema.getFields()) {
                        String columnName = field.name();
                        record.put(columnName, rowData.get(columnIndex));
                        columnIndex++;
                    }

                    writer.write(record);
                    recordCount++;
                    log.debug("Total Records generated= " + (recordCount - 1));
                }
            } catch (Exception e) {
                throw new IOException("error while writing", e);
            }
        }
        out.close();
    }

    @Override
    public void write(VOTableDocument tm, Writer out) {
        throw new UnsupportedOperationException("This method for Parquet Writer is not supported.");
    }

    @Override
    public void write(VOTableDocument resultSet, Writer out, Long maxRec) {
        throw new UnsupportedOperationException("This method for Parquet Writer is not supported.");
    }

    @Override
    public void write(Throwable thrown, OutputStream output) throws IOException {
        //TODO: Implement
    }

    private static OutputFile outputFileFromStream(OutputStream outputStream) {
        return new OutputFile() {
            @Override
            public PositionOutputStream create(long blockSizeHint) {
                return new PositionOutputStream() {
                    private long position = 0;

                    @Override
                    public void write(int b) {
                        try {
                            outputStream.write(b);
                            position++;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void write(byte[] b, int off, int len) {
                        try {
                            outputStream.write(b, off, len);
                            position += len;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public long getPos() {
                        return position;
                    }

                    @Override
                    public void close() {
                        try {
                            outputStream.close();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
            }

            @Override
            public PositionOutputStream createOrOverwrite(long blockSizeHint) {
                return create(blockSizeHint);
            }

            @Override
            public boolean supportsBlockSize() {
                return false;
            }

            @Override
            public long defaultBlockSize() {
                return 0;
            }
        };
    }
}
