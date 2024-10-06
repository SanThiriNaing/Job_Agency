package job_agency.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import job_agency.emailService.EmailService;
import job_agency.emailService.OtpService;
import job_agency.emailService.OtpStorageService;
import job_agency.model.User;
import job_agency.service.UserService;


@Controller
public class RegistrationController {

	@Autowired
    private UserService userService;
	
	@Autowired
	private EmailService emailService;
	
	  @Autowired
	private OtpStorageService otpStorageService;
	  
	  @Autowired
	private OtpService otpService;
	  
	  @RequestMapping("/showregister")
	    public String showRegistrationForm(Model model) {
	        model.addAttribute("user", new User());
	        return "register"; 
	    }
	  
	  @PostMapping("/doRegister")
	    public ModelAndView registerUser(@ModelAttribute("user") User user, HttpSession session) {
	        ModelAndView mav = new ModelAndView();
	        
	        // Check if the user already exists
	        if (userService.checkEmail(user.getEmail())) {
	            mav.setViewName("register");
	            mav.addObject("EmailExist", "Email already registered.");
	            return mav;
	        }

	       

	        // Store user details in session
	        session.setAttribute("userDetails", user);
	        
	        String otp =  otpService.generateOtp();
			  otpStorageService.storeOtp(user.getEmail(), otp);
			  emailService.sendOtpEmail(user.getEmail(), otp);



	        // Store email for OTP verification
	        session.setAttribute("pendingEmail", user.getEmail());

	        mav.setViewName("verify-otp");
	        mav.addObject("message", "OTP has been sent to your email.");
	        return mav;
	    }
	  
	  @PostMapping("/verifyOtp")
	    public ModelAndView verifyOtp(@RequestParam("otp") String otp, HttpSession session) {
	        ModelAndView mav = new ModelAndView();
	        String pendingEmail = (String) session.getAttribute("pendingEmail");

	       User user = (User) session.getAttribute("userDetails");
	       
	       
	       if(user==null) {
	    	   mav.setViewName("register");
	            mav.addObject("sessionExpired", "Session expired. Please register again.");
	            return mav;
	    	   
	       }

	        if (pendingEmail == null) {
	            mav.setViewName("register");
	            mav.addObject("sessionExpired", "Session expired. Please register again.");
	            return mav;
	        }

	        boolean isValid = otpStorageService.validateOtp(pendingEmail, otp);
	        if (isValid) {
	        	userService.insertUser(user);
	        	
	           
	            mav.setViewName("sucess");
	        } else {
	            mav.setViewName("verify-otp");
	            mav.addObject("errorOtp", "Invalid or expired OTP.");
	        }

	        return mav;
	    }
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
//	  public void generateAndSendOTP(User user) {
//		  String otp =  otpService.generateOtp();
//		  otpStorageService.storeOtp(user.getEmail(), otp);
//		  emailService.sendOtpEmail(user.getEmail(), otp);
//		
//		  
//	  }
//	 
	}