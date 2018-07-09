package io.jawg.osmcontributor.rest.dtos.osm;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;


@Root(name = "member", strict = false)
public class RelationMemberDto {

    @Attribute(name = "type")
    private String type;

    @Attribute(name = "ref")
    private Long ref;

    @Attribute(name = "role")
    private String role;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getRef() {
        return ref;
    }

    public void setRef(Long ref) {
        this.ref = ref;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "RelationMemberDto{" +
                "type='" + type + '\'' +
                ", ref=" + ref +
                ", role='" + role + '\'' +
                '}';
    }
}
