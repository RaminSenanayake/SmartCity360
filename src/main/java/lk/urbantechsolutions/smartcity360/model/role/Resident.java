package lk.urbantechsolutions.smartcity360.model.role;

public class Resident {
    private String name;
    private ComplaintSeverity complaintSeverity;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ComplaintSeverity getComplaintSeverity() {
        return complaintSeverity;
    }

    public void setComplaintSeverity(ComplaintSeverity complaintSeverity) {
        this.complaintSeverity = complaintSeverity;
    }
}
