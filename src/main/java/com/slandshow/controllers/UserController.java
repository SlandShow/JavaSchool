package com.slandshow.controllers;

import com.slandshow.DTO.*;
import com.slandshow.models.Schedule;
import com.slandshow.service.*;
import com.slandshow.utils.JspFormNames;
import com.slandshow.utils.UtilsManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@Controller
public class UserController {

    private static final Logger LOGGER = Logger.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private SecureService secureService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private StationService stationService;

    @GetMapping("/login")
    public String login(Model model) {

        return "login";
    }


    public String registration() {

        return "registration";
    }


   // @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/buyTicket")
    public String buyTicket(Model model) {
        model.addAttribute("ticket", new TicketInfoDTO());
        return JspFormNames.BOOKING_TICKET_FORM;
    }

    @PostMapping("/buyTicket")
    public String confirmTicketBooking(@ModelAttribute TicketInfoDTO ticketInfoDTO, HttpServletRequest request, Model model) {
        Schedule schedule = new Schedule();

        if (
                ticketInfoDTO.getScheduleStationDepartureName() == null ||
                ticketInfoDTO.getScheduleStationArrivalName() == null ||
                ticketInfoDTO.getScheduleStationDepartureName() == null ||
                ticketInfoDTO.getUserLogin() == null ||
                ticketInfoDTO.getSeatCarriage() < 0 ||
                ticketInfoDTO.getSeatSeat() < 0
                ) {
            System.out.println("NOT VALID INPUT!");
            return JspFormNames.BOOKING_TICKET_FORM;
        }

        schedule.setStationDeparture(
           stationService.getStationByName(ticketInfoDTO.getScheduleStationDepartureName())
        );

        schedule.setStationArrival(
           stationService.getStationByName(ticketInfoDTO.getScheduleStationArrivalName())
        );

        try {
            schedule.setDateDeparture(
                    UtilsManager.parseToDateTime(ticketInfoDTO.getScheduleDateDeparture())
            );
        } catch (ParseException e) {
            e.printStackTrace();
        }

        TicketInfoDTO parsedTicketInfo = new TicketInfoDTO();
        parsedTicketInfo.setUserLogin(ticketInfoDTO.getUserLogin());
        parsedTicketInfo.setSeatSeat(ticketInfoDTO.getSeatSeat());
        parsedTicketInfo.setSeatCarriage(ticketInfoDTO.getSeatCarriage());

        request.getSession().setAttribute("ticketDTO", parsedTicketInfo);
        model.addAttribute("schedules", scheduleService.getByStationsAndDate(schedule));

        return JspFormNames.BOOKING_TICKET_LIST;
    }

//    @GetMapping("/bookingTicket/{id}")
    @RequestMapping(value = "/bookingTicket", params = "id")
    public String confirmBooking(@RequestParam(value = "id") Long id,  HttpServletRequest request) {
        TicketDTO ticketDTO = new TicketDTO();
        ticketDTO.setScheduleId(id);
        SeatDTO seatDTO = new SeatDTO();

        TicketInfoDTO ticketInfoDTO = (TicketInfoDTO) request.getSession().getAttribute("ticketDTO");

        seatDTO.setSeat(ticketInfoDTO.getSeatSeat());
        seatDTO.setCarriage(ticketInfoDTO.getSeatCarriage());
        ticketDTO.setSeatDTO(seatDTO);

        LOGGER.info("TICKET DTO: " + ticketInfoDTO);
        LOGGER.info("ID OF CURRENT SCHEDULE IS " + id + " USER LOGIN IS " + ticketInfoDTO.getUserLogin() + " SEAT N IS " + ticketInfoDTO.getSeatSeat() + " CARRIAGE N IS " + ticketInfoDTO.getSeatCarriage());


        try {
            ticketService.add(
                    ticketDTO,
                    userService.findUserByEmail(ticketInfoDTO.getUserLogin())
            );
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return JspFormNames.BOOKING_TICKET_FORM_RESULT;
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin")
    public String admin() {

        return "admin";
    }


/*
    @PostMapping("/registration")
    public void registration(@RequestBody UserDTO userDTO) throws IOException {
        userService.registration(userDTO);
    }*/

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/home/profile/get")
    public ResponseEntity<?> getProfile() {
        UserDTO userDTO = userService.findAuthenticatedUserDTO();
        return ResponseEntity.ok(userDTO);
    }


    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("/home/update")
    public void updateUser(@RequestBody UserDTO userDTO) throws ParseException {
        userService.updateProfile(userDTO);
    }
/*
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(URLs.GET_USERS_TICKETS)
    public ResponseEntity<?> showTrips() {
        List<TicketInfoDTO> tickets = ticketService.getAuthenticatedUserTicket();
        return ResponseEntity.ok(tickets);
    }

    /*
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(URLs.AUDIT)
    public ResponseEntity<?> getAuditInfo() {
        List<AuditDTO> auditDTOList = auditService.getAuditsInfo();
        return ResponseEntity.ok(auditDTOList);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(URLs.DOWNLOAD)
    public void downloadTicket(HttpServletResponse response,
                               @PathVariable Long id) throws IOException, DocumentException {
        Ticket ticket = ticketService.getById(id);
        File file = ticketBuilderPDF.createPDF(ticket);
        String mimeType = URLConnection.guessContentTypeFromName(file.getName());
        response.setContentType(mimeType);
        response.setContentLength((int) file.length());
        response.setHeader("Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));
        InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        FileCopyUtils.copy(inputStream, response.getOutputStream());
    }

    @PostMapping(URLs.AUTH)
    public ResponseEntity<?> authInfo(@RequestBody UserDTO userDTO) {
        UsernamePasswordAuthenticationToken authenticationToken = secureService.authenticate(userDTO);
        return ResponseEntity.ok(authenticationToken.getAuthorities());
    } */
}