package org.openmrs.module.registrationapp.page.controller;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.registrationapp.AddressSupportCompatibility;
import org.openmrs.module.registrationapp.NameSupportCompatibility;
import org.openmrs.module.registrationapp.RegistrationAppUiUtils;
import org.openmrs.module.registrationapp.form.RegisterPatientFormBuilder;
import org.openmrs.module.registrationapp.model.NavigableFormStructure;
import org.openmrs.module.registrationapp.model.Question;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Collections;

public class RegisterPatientPageController extends AbstractRegistrationAppPageController {

    public void get(UiSessionContext sessionContext, PageModel model,
                    @RequestParam("appId") AppDescriptor app,
                    @RequestParam(value = "breadcrumbOverride", required = false) String breadcrumbOverride,
                    @RequestParam(value="returnUrl", required=false) String returnUrl,
                    @RequestParam(value = "initialValues", required = false)  String initialValues,
                    @RequestParam(value = "goToSectionId", required = false)  String goToSectionId,
                    @RequestParam(value = "mother", required = false)  Patient mother,
                    @ModelAttribute("patient") @BindParams Patient patient,
                    @SpringBean("emrApiProperties") EmrApiProperties emrApiProperties,
                    @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService,
                    UiUtils ui) throws Exception {

        sessionContext.requireAuthentication();
        addModelAttributes(model, patient, app, emrApiProperties.getPrimaryIdentifierType(), breadcrumbOverride, returnUrl, initialValues, goToSectionId, mother,appFrameworkService, sessionContext);
    }

    public void addModelAttributes(PageModel model, Patient patient, AppDescriptor app, PatientIdentifierType primaryIdentifierType, String breadcrumbOverride, String returnUrl, String initialValues, String goToSectionId, Patient mother, AppFrameworkService appFrameworkService, UiSessionContext sessionContext) throws Exception {
        NavigableFormStructure formStructure = RegisterPatientFormBuilder.buildFormStructure(app, app.getConfig().get("combineSections") != null ? app.getConfig().get("combineSections").getBooleanValue() : false,
                appFrameworkService, sessionContext.generateAppContextModel());

        if (patient == null) {
        	patient = new Patient();
        }

        NameSupportCompatibility nameSupport = Context.getRegisteredComponent(NameSupportCompatibility.ID, NameSupportCompatibility.class);
        AddressSupportCompatibility addressSupport = Context.getRegisteredComponent(AddressSupportCompatibility.ID, AddressSupportCompatibility.class);
        
        model.addAttribute("patient", patient);
        model.addAttribute("primaryIdentifierType", primaryIdentifierType);
        model.addAttribute("appId", app.getId());
        model.addAttribute("formStructure", formStructure);
        model.addAttribute("nameTemplate", nameSupport.getDefaultLayoutTemplate());
        model.addAttribute("addressTemplate", addressSupport.getAddressTemplate());
        model.addAttribute("includeRegistrationDateSection", !app.getConfig().get("registrationEncounter").isNull()
                && !app.getConfig().get("allowRetrospectiveEntry").isNull()
                && app.getConfig().get("allowRetrospectiveEntry").getBooleanValue() );
        model.addAttribute("allowUnknownPatients", app.getConfig().get("allowUnknownPatients").getBooleanValue());
        model.addAttribute("allowManualIdentifier", app.getConfig().get("allowManualIdentifier").getBooleanValue());
        model.addAttribute("patientDashboardLink", app.getConfig().get("patientDashboardLink") !=null ?
                app.getConfig().get("patientDashboardLink").getTextValue() : null);
        model.addAttribute("combineSubSections", app.getConfig().get("combineSubSections") !=null ?
        		app.getConfig().get("combineSubSections").getBooleanValue() : false);
        model.addAttribute("enableOverrideOfAddressPortlet",
                Context.getAdministrationService().getGlobalProperty("addresshierarchy.enableOverrideOfAddressPortlet", "false"));
        model.addAttribute("breadcrumbOverride", breadcrumbOverride);
        model.addAttribute("returnUrl", returnUrl);
        model.addAttribute("initialFieldValues", initialValues);
        model.addAttribute("goToSectionId", goToSectionId);
        model.addAttribute("mother", mother);
        model.addAttribute("relationshipTypes", Context.getPersonService().getAllRelationshipTypes());

        List<Extension> includeFragments = appFrameworkService.getExtensionsForCurrentUser("registerPatient.includeFragments");
        Collections.sort(includeFragments);
        model.addAttribute("includeFragments", includeFragments);
        model.addAttribute("genderOptions", RegistrationAppUiUtils.getGenderOptions(app));
    }
}
