import _allListSources from "./allListSources.json";
import _allMessages from "./allMessages.json";
import _allValueSchemas from "./allValueSchemas.json";

import _department from './form/department.form.json';
import _institute from './form/institute.form.json';
import _role from './form/role.form.json';
import _trust from './form/trust.form.json';
import _user from './form/user.form.json';
import _userRole from './form/userRole.form.json';

export const generatedArtifacts = {
	allListSources: _allListSources,
	allMessages: _allMessages,
	allValueSchemas: _allValueSchemas,
	allForms: {
		'department': _department,
		'institute': _institute,
		'role': _role,
		'trust': _trust,
		'user': _user,
		'userRole': _userRole
	}
};

