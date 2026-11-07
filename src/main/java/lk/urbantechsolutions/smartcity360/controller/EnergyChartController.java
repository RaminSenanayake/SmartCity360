package lk.urbantechsolutions.smartcity360.controller;

import lk.urbantechsolutions.smartcity360.model.zone.ZoneType;
import lk.urbantechsolutions.smartcity360.security.PortalUser;
import lk.urbantechsolutions.smartcity360.service.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.util.List;

@RestController
public class EnergyChartController {
    private final PortalDataService data;
    private final PowerControlService power;

    public EnergyChartController(PortalDataService data, PowerControlService power) { this.data = data; this.power = power; }

    @GetMapping("/api/usage")
    public List<PortalDataService.EnergyPoint> usage(@RequestParam(required = false) Long cityId,
                                                   @RequestParam(defaultValue = "CITY") ZoneType level,
                                                   @AuthenticationPrincipal PortalUser user) {
        return data.chart(cityId, level, user);
    }

    @GetMapping(value = "/staff/meters/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> meter(@PathVariable Long id, @AuthenticationPrincipal PortalUser user) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(power.getUsage(id, ZoneType.BUILDING, user.getStaffIdentity(), null, false, false));
    }
}
