package org.simplity.fm.example.gen.form;

import org.simplity.fm.core.app.App;
import org.simplity.fm.core.data.Form;
import org.simplity.fm.core.data.LinkMetaData;
import org.simplity.fm.core.data.LinkedForm;
import org.simplity.fm.example.gen.rec.UserRecord;
/** class for form user  */
public class UserForm extends Form<UserRecord> {
	protected static final String NAME = "user";
	protected static final UserRecord RECORD = (UserRecord) App.getApp().getCompProvider().getRecord("user");
	protected static final  boolean[] OPS = {true, false, false, false, true};
	private static final LinkedForm<?>[] LINKS = null;
/** constructor */
public UserForm() {
		super(NAME, RECORD, OPS, LINKS);
	}
}
