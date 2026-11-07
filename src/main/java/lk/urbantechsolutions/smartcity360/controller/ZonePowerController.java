package lk.urbantechsolutions.smartcity360.controller;

import jakarta.persistence.EntityNotFoundException;
import lk.urbantechsolutions.smartcity360.model.zone.ZoneType;
import lk.urbantechsolutions.smartcity360.security.PortalUser;
import lk.urbantechsolutions.smartcity360.service.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/power")
public class ZonePowerController {
    private final ZonePowerService zones;
    private final PowerControlService power;
    private final PortalDataService data;

    public ZonePowerController(ZonePowerService zones, PowerControlService power, PortalDataService data) {
        this.zones = zones; this.power = power; this.data = data;
    }

    @ModelAttribute("portalUser")
    public PortalUser user(@AuthenticationPrincipal PortalUser user) { return user; }

    @GetMapping
    public String controls(@RequestParam(required = false) Long cityId, Model model) {
        model.addAttribute("zones", zones.getZones(cityId));
        model.addAttribute("cities", data.cityChoices());
        model.addAttribute("selectedCityId", cityId);
        model.addAttribute("summary", data.summary(cityId));
        return "zone-power";
    }

    @PostMapping
    public String setPower(@RequestParam Long zoneId, @RequestParam ZoneType zoneType,
                           @RequestParam boolean powerOn, @RequestParam(required = false) Long cityId,
                           RedirectAttributes redirect) {
        if (powerOn) power.turnOnPower(zoneId, zoneType); else power.turnOffPower(zoneId, zoneType);
        redirect.addFlashAttribute("success", "Zone power turned " + (powerOn ? "on" : "off") + ". Child zones have been updated.");
        if (cityId != null) redirect.addAttribute("cityId", cityId);
        return "redirect:/staff/power";
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.NOT_FOUND)
    public String notFound(Model model) { model.addAttribute("status", 404); return "error"; }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
    public String invalidZone(Model model) { model.addAttribute("status", 400); return "error"; }
}
