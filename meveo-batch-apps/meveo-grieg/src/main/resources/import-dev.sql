
insert into COM_MESSAGE_TEMPLATE (ID,PROVIDER_ID,VERSION,MEDIA,CREATED,DISABLED,CODE,TAG_END,TAG_START,TYPE,HTMLCONTENT,SUBJECT,TEXTCONTENT)
values (1,1,0,'EMAIL',SYSDATE,0,'PART_RAPPEL','>','#<','DUNNING',null,'RAPPEL #<LETTER_DATE>','Monsieur,'||CHR(9) || CHR(10)||CHR(9) || CHR(10)||' A ce jour et sauf erreur de notre part, vous restez nous devoir la somme de #<AMOUNT_WITH_TAX>Ä.'||CHR(9) || CHR(10)||' Si vous avez effectuÈ votre paiement rÈcemment, nous vous prions de ne pas tenir compte de cette relance.'||CHR(9) || CHR(10)||'S''il s''agit d''un oubli, nous vous invitons ‡ rÈgler votre facture dËs aujourd''hui par tout moyen de paiement ‡ votre convenance.'||CHR(9) || CHR(10)||' A dÈfaut de rËglement dans un dÈlai de 15 jours, une pÈnalitÈ de 10,00 Ä minimum viendra s''ajouter ‡ votre solde d˚.'||CHR(9) || CHR(10)||'En cas de difficultÈ ou pour toute autre raison, n''hÈsitez pas ‡ contacter rapidement notre service clientËle aux numÈros et horaires prÈcisÈs en haut ‡ gauche de cette lettre.'||CHR(9) || CHR(10)||'Dans l''attente de votre rËglement, nous vous prions d''agrÈer, Monsieur, l''exression de nos sentiments dÈvouÈs.'||CHR(9) || CHR(10)||CHR(9) || CHR(10)||'Le Service Client Dolce ‘ "');
insert into COM_MESSAGE_TEMPLATE (ID,PROVIDER_ID,VERSION,MEDIA,CREATED,DISABLED,CODE,TAG_END,TAG_START,TYPE,HTMLCONTENT,SUBJECT,TEXTCONTENT)
values (2,1,0,'EMAIL',SYSDATE,0,'PART_MISDEM','>','#<','DUNNING',null,'MISE EN DEMEURE #<LETTER_DATE>','Cher #<CUSTOMER_NAME>,'||CHR(9) || CHR(10)||'Vous n''avez pas réglé votre facture du #<INVOICE_DATE>, nous devez #<AMOUNT_WITH_TAX> euros à ce jour.' );
insert into COM_MESSAGE_TEMPLATE (ID,PROVIDER_ID,VERSION,MEDIA,CREATED,DISABLED,CODE,TAG_END,TAG_START,TYPE,HTMLCONTENT,SUBJECT,TEXTCONTENT)
values (3,1,0,'EMAIL',SYSDATE,0,'PART_DERNAV','>','#<','DUNNING','<html><h1>Cher #<CUSTOMER_NAME>,</h1><p>Vous n''avez pas réglé votre facture du <b>#<INVOICE_DATE></b>, nous devez #<AMOUNT_WITH_TAX> euros à ce jour.</p></html>','Votre facture du #<INVOICE_DATE>',null );

insert into COM_SENDER_CONFIG (ID,VERSION,CREATED,DISABLED,PROVIDER_ID,CODE,MEDIA,SMTP_HOST,SMTP_PORT,LOGIN,PASSWORD,DELAY_MIN,DEFAULT_FROM_EMAIL,DEFAULT_REPLY_EMAIL)
values (1,0,SYSDATE,0,1,'MANATY SMTP','EMAIL','zimbra.manaty.net','25','projetmeveo','projetMEVEO',5000,'toto@gmail.com','reply@gmail.com');

delete from COM_MSG_VAR_VALUE;
delete from COM_MESSAGE;
delete from COM_CAMPAIGN;

insert into COM_CAMPAIGN (ID,VERSION,CREATED,DISABLED,PROVIDER_ID,CODE,SCHEDULE_DATE,STATUS)
values (1,0,SYSDATE,0,1,'TEST',sysdate,'SCHEDULED');

insert into COM_MESSAGE (ID,VERSION,PROVIDER_ID,TEMPLATECODE,CAMPAIGN_ID,MEDIA,STATUS)
values (1,0,1,'PART_RAPPEL',1,'EMAIL','WAITING');

insert into COM_MSG_VAR_VALUE (ID,VERSION,CREATED,DISABLED,PROVIDER_ID,MESSAGE,CODE,VALUE)
values (1,0,sysdate,0,1,1,'RECIPIENT_ADDRESS','smichea@gmail.com');
insert into COM_MSG_VAR_VALUE (ID,VERSION,CREATED,DISABLED,PROVIDER_ID,MESSAGE,CODE,VALUE)
values (2,0,sysdate,0,1,1,'CUSTOMER_NAME','M SEBASTIEN MICHEA');
insert into COM_MSG_VAR_VALUE (ID,VERSION,CREATED,DISABLED,PROVIDER_ID,MESSAGE,CODE,VALUE)
values (3,0,sysdate,0,1,1,'LETTER_DATE','01/01/2011');
insert into COM_MSG_VAR_VALUE (ID,VERSION,CREATED,DISABLED,PROVIDER_ID,MESSAGE,CODE,VALUE)
values (4,0,sysdate,0,1,1,'AMOUNT_WITH_TAX','136,2');

