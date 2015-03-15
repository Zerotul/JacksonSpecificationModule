package org.zerotul.specification.jackson.test.mock;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

/**
 * Created by zerotul on 13.03.15.
 */
@JsonTypeName("mock")
public class Mock implements Serializable {

    private String field1;
    private String field2;
    private String field3;
    private int field4;
    private Mock mock;
    private Long id;

    public String getField1() {
        return field1;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    public String getField2() {
        return field2;
    }

    public void setField2(String field2) {
        this.field2 = field2;
    }

    public String getField3() {
        return field3;
    }

    public void setField3(String field3) {
        this.field3 = field3;
    }

    public int getField4() {
        return field4;
    }

    public void setField4(int field4) {
        this.field4 = field4;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Mock getMock() {
        return mock;
    }

    public void setMock(Mock mock) {
        this.mock = mock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Mock mock1 = (Mock) o;

        if (field4 != mock1.field4) return false;
        if (field1 != null ? !field1.equals(mock1.field1) : mock1.field1 != null) return false;
        if (field2 != null ? !field2.equals(mock1.field2) : mock1.field2 != null) return false;
        if (field3 != null ? !field3.equals(mock1.field3) : mock1.field3 != null) return false;
        if (id != null ? !id.equals(mock1.id) : mock1.id != null) return false;
        if (mock != null ? !mock.equals(mock1.mock) : mock1.mock != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = field1 != null ? field1.hashCode() : 0;
        result = 31 * result + (field2 != null ? field2.hashCode() : 0);
        result = 31 * result + (field3 != null ? field3.hashCode() : 0);
        result = 31 * result + field4;
        result = 31 * result + (mock != null ? mock.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
