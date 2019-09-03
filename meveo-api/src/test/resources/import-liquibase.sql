

insert into CAT_ONE_SHOT_CHARGE_TEMPL (id, type, immediate_invoicing) values (2, 'SUBSCRIPTION', false);
insert into CAT_ONE_SHOT_CHARGE_TEMPL (id, type, immediate_invoicing) values (4, 'SUBSCRIPTION', false);

insert into CAT_SERVICE_TEMPLATE (id, version, disabled, created, code, description, provider_id) values (1, 0, false, now(), 'SERV1', 'SERV1', 1);

DROP SEQUENCE IF EXISTS CAT_SERVICE_TEMPLATE_SEQ;
CREATE SEQUENCE CAT_SERVICE_TEMPLATE_SEQ start with 2 increment by 1;







insert into crm_customer (id, CUSTOMER_CATEGORY_ID, CUSTOMER_BRAND_ID, SELLER_ID) values (1, 2, 1, 1);




/* Custom Fields */
insert into CRM_CUSTOM_FIELD_TMPL (id, version, disabled, created, code, description, account_type, field_type, value_required, provider_id, creator_id) values (1, 0, false, now(), 'SOAP_CUST10', 'SOAP_CUST10', 'CUST', 'STRING', false, 1, 1);
insert into CRM_CUSTOM_FIELD_TMPL (id, version, disabled, created, code, description, account_type, field_type, value_required, provider_id, creator_id) values (2, 0, false, now(), 'SOAP_CA10', 'SOAP_CA10', 'CA', 'DATE', false, 1, 1);
insert into CRM_CUSTOM_FIELD_TMPL (id, version, disabled, created, code, description, account_type, field_type, value_required, provider_id, creator_id) values (3, 0, false, now(), 'SOAP_BA10', 'SOAP_BA10', 'BA', 'LONG', false, 1, 1);
insert into CRM_CUSTOM_FIELD_TMPL (id, version, disabled, created, code, description, account_type, field_type, value_required, provider_id, creator_id) values (4, 0, false, now(), 'SOAP_UA10', 'SOAP_UA10', 'UA', 'DOUBLE', false, 1, 1);
insert into CRM_CUSTOM_FIELD_TMPL (id, version, disabled, created, code, description, account_type, field_type, value_required, provider_id, creator_id) values (5, 0, false, now(), 'SOAP_SUB10', 'SOAP_SUB10', 'SUB', 'STRING', false, 1, 1);

DROP SEQUENCE IF EXISTS CRM_CUSTOM_FLD_TMP_SEQ;
CREATE SEQUENCE CRM_CUSTOM_FLD_TMP_SEQ start with 6 increment by 1;