package art.settings;

import art.enums.ArtAuthenticationMethod;
import art.enums.DisplayNull;
import art.enums.LdapAuthenticationMethod;
import art.enums.LdapConnectionEncryptionMethod;
import art.enums.PdfPageSize;
import art.servlets.ArtConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletContext;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for settings configuration
 *
 * @author Timothy Anyona
 */
@Controller
//put form object in session attributes so that properties that are not included
//in the form are retained when the form is posted. Otherwise they will be null.
@SessionAttributes("settings")
public class SettingsController {

	final static org.slf4j.Logger logger = LoggerFactory.getLogger(SettingsController.class);
	
	@Autowired
	private ServletContext ctx;
	
	@ModelAttribute("pdfPageSizes")
	public PdfPageSize[] addPdfPageSizes() {
		return PdfPageSize.values();
	}

	@ModelAttribute("ldapConnectionEncryptionMethods")
	public LdapConnectionEncryptionMethod[] addLdapConnectionEncryptionMethods() {
		return LdapConnectionEncryptionMethod.values();
	}

	@ModelAttribute("artAuthenticationMethods")
	public List<ArtAuthenticationMethod> addArtAuthenticationMethods() {
		List<ArtAuthenticationMethod> methods = new ArrayList<ArtAuthenticationMethod>();
		methods.addAll(Arrays.asList(ArtAuthenticationMethod.values()));

		//remove irrelevant methods
		methods.remove(ArtAuthenticationMethod.Custom);
		methods.remove(ArtAuthenticationMethod.Public);
		methods.remove(ArtAuthenticationMethod.Repository);

		return methods;
	}

	@ModelAttribute("ldapAuthenticationMethods")
	public LdapAuthenticationMethod[] addLdapAuthenticationMethods() {
		return LdapAuthenticationMethod.values();
	}
	
	@ModelAttribute("displayNullOptions")
	public DisplayNull[] addDisplayNullOptions(){
		return DisplayNull.values();
	}
	

	@RequestMapping(value = "app/settings", method = RequestMethod.GET)
	public String showSettings(Model model) {
		Settings settings = ArtConfig.getSettings();

		//save current smtp and ldap bind password for use in POST
		settings.setCurrentSmtpPassword(settings.getSmtpPassword());
		settings.setCurrentLdapBindPassword(settings.getLdapBindPassword());

		model.addAttribute("settings", settings);

		return "settings";
	}

	@RequestMapping(value = "app/settings", method = RequestMethod.POST)
	public String processSettings(
			@ModelAttribute("settings") @Valid Settings settings,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			SessionStatus sessionStatus) {

		if (result.hasErrors()) {
			model.addAttribute("formErrors", "true");
			return "settings";
		}

		//set password field as appropriate
		String newSmtpPassword = settings.getSmtpPassword();
		if (settings.isUseBlankSmtpPassword()) {
			newSmtpPassword = "";
		} else {
			if (StringUtils.isEmpty(newSmtpPassword)) {
				//use current password
				newSmtpPassword = settings.getCurrentSmtpPassword();
			}
		}
		settings.setSmtpPassword(newSmtpPassword);

		String newLdapBindPassword = settings.getLdapBindPassword();
		if (settings.isUseBlankLdapBindPassword()) {
			newLdapBindPassword = "";
		} else {
			if (StringUtils.isEmpty(newLdapBindPassword)) {
				newLdapBindPassword = settings.getCurrentLdapBindPassword();
			}
		}
		settings.setLdapBindPassword(newLdapBindPassword);

		try {
			ArtConfig.saveSettings(settings);
			
			//save administrator email in application context. for display in footer
			ctx.setAttribute("administratorEmail", settings.getAdministratorEmail());
			
			//clear SessionAttributes
			sessionStatus.setComplete();

			//use redirect after successful submission 
			redirectAttributes.addFlashAttribute("success", "true");
			return "redirect:/app/settings.do";
		} catch (IOException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "settings";
	}
}
