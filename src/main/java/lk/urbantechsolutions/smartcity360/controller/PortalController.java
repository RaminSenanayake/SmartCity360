package lk.urbantechsolutions.smartcity360.controller;

import lk.urbantechsolutions.smartcity360.security.PortalUser;
import lk.urbantechsolutions.smartcity360.service.*;
import lk.urbantechsolutions.smartcity360.repository.ComplaintRepository;
import lk.urbantechsolutions.smartcity360.model.resident.*;
import lk.urbantechsolutions.smartcity360.model.policy.PolicyContext;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalTime;

@Controller
public class PortalController {
    private final PortalDataService data;
    private final ComplaintRepository complaints;
    private final ComplaintHandlingService complaintService;
    private final CityPolicyService policies;

    public PortalController(PortalDataService data, ComplaintRepository complaints,
                            ComplaintHandlingService complaintService, CityPolicyService policies) {
        this.data = data; this.complaints = complaints; this.complaintService = complaintService; this.policies = policies;
    }

    @GetMapping("/")
    public String home() { return "redirect:/dashboard"; }

    @GetMapping("/sign-in")
    public String signIn(@AuthenticationPrincipal PortalUser user,
                         @RequestParam(defaultValue = "resident") String portal, Model model) {
        model.addAttribute("selectedPortal", "staff".equals(portal) ? "staff" : "resident");
        return user == null ? "sign-in" : "redirect:/dashboard";
    }

    @ModelAttribute("portalUser")
    public PortalUser user(@AuthenticationPrincipal PortalUser user) { return user; }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal PortalUser user, @RequestParam(required = false) Long cityId, Model model) {
        model.addAttribute("page", "dashboard");
        model.addAttribute("cities", data.cityChoices());
        model.addAttribute("selectedCityId", cityId);
        model.addAttribute("summary", data.summary(cityId));
        model.addAttribute("recentComplaints", complaintList(user).stream().limit(4).toList());
        if (user.isStaff()) model.addAttribute("buildings", data.buildingChoices(cityId, user));
        return "dashboard";
    }

    private java.util.List<Complaint> complaintList(PortalUser user) {
        return user.isStaff() ? complaints.findAll(org.springframework.data.domain.Sort.by("createdAt").descending())
                : complaints.findByResident_IdOrderByCreatedAtDesc(user.getSubjectId());
    }

    @GetMapping("/complaints")
    public String complaints(@AuthenticationPrincipal PortalUser user, Model model) {
        model.addAttribute("page", "complaints");
        model.addAttribute("complaints", complaintList(user));
        model.addAttribute("severities", ComplaintSeverity.values());
        return "complaints";
    }

    @PostMapping("/resident/complaints")
    public String submitComplaint(@AuthenticationPrincipal PortalUser user, @RequestParam String text,
                                  RedirectAttributes redirect) {
        if (text.isBlank() || text.trim().length() > 255) {
            redirect.addFlashAttribute("error", "Describe the issue in 1-255 characters.");
        } else {
            complaintService.submitComplaint(user.getSubjectId(), text.trim());
            redirect.addFlashAttribute("success", "Your complaint has been submitted successfully.");
        }
        return "redirect:/complaints";
    }

    @PostMapping("/staff/complaints/{id}/severity")
    public String assessComplaint(@PathVariable Long id, @RequestParam ComplaintSeverity severity,
                                  @AuthenticationPrincipal PortalUser user, RedirectAttributes redirect) {
        try {
            complaintService.assessComplaint(id, severity, user.getStaffIdentity(), null);
            redirect.addFlashAttribute("success", "Severity updated and the complaint routed to the appropriate team.");
        } catch (IllegalStateException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/complaints";
    }

    @GetMapping("/policies")
    public String policies(Model model) {
        model.addAttribute("page", "policies");
        model.addAttribute("policies", policies.getAll());
        return "policies";
    }

    @PostMapping("/staff/policies")
    public String savePolicy(@RequestParam(required = false) Long id, @RequestParam String name,
                             @RequestParam String rule, RedirectAttributes redirect) {
        try {
            if (name.length() > 255) throw new IllegalArgumentException("Policy name must be at most 255 characters");
            if (id == null) policies.add(name, rule); else policies.update(id, name, rule);
            redirect.addFlashAttribute("success", "Policy saved. The updated rule applies to the next evaluation.");
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
            redirect.addFlashAttribute("draftName", name);
            redirect.addFlashAttribute("draftRule", rule);
            redirect.addFlashAttribute("draftId", id);
        }
        return "redirect:/policies";
    }

    @PostMapping("/staff/policies/{id}/delete")
    public String deletePolicy(@PathVariable Long id, RedirectAttributes redirect) {
        policies.remove(id);
        redirect.addFlashAttribute("success", "Policy deleted.");
        return "redirect:/policies";
    }

    @PostMapping("/staff/policies/preview")
    public String preview(@RequestParam String rule, @RequestParam BigDecimal noise,
                          @RequestParam LocalTime time, RedirectAttributes redirect) {
        redirect.addFlashAttribute("previewRule", rule);
        redirect.addFlashAttribute("previewNoise", noise);
        redirect.addFlashAttribute("previewTime", time);
        try {
            boolean result = policies.preview(rule, new PolicyContext(noise, time));
            redirect.addFlashAttribute("previewResult", result ? "The supplied facts satisfy this policy." : "The supplied facts do not satisfy this policy.");
            redirect.addFlashAttribute("previewPass", result);
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/policies";
    }

    @RequestMapping("/access-denied")
    @ResponseStatus(org.springframework.http.HttpStatus.FORBIDDEN)
    public String accessDenied(Model model) { model.addAttribute("page", ""); return "access-denied"; }
}
