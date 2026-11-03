package lk.urbantechsolutions.smartcity360.service.impl;

import lk.urbantechsolutions.smartcity360.model.role.LocalStaff;
import lk.urbantechsolutions.smartcity360.model.role.Resident;
import lk.urbantechsolutions.smartcity360.repository.LocalStaffRepository;
import lk.urbantechsolutions.smartcity360.service.ComplaintHandlingService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class ComplaintHandlingServiceImpl implements ComplaintHandlingService {

    private LocalStaffRepository localStaffRepository;

    public ComplaintHandlingServiceImpl(LocalStaffRepository localStaffRepository) {
        this.localStaffRepository = localStaffRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public void handleComplaint(Resident resident) {
        long count = localStaffRepository.count();
        if (count == 0) {
            throw new IllegalStateException("No local staff available to handle the complaint");
        }

        int randomIndex = ThreadLocalRandom.current().nextInt((int) count);

        LocalStaff staff = localStaffRepository
                .findAll(PageRequest.of(randomIndex, 1))
                .getContent()
                .get(0);

        staff.handleComplaint(resident);
    }
}
