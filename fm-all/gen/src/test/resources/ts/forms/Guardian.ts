
import { Form , Field, ChildForm } from '../form/form';
import { SelectOption, Vo } from '../form/types';
import { Validators } from '@angular/forms'

export class Guardian extends Form {
	private static _instance = new Guardian();
	guardianId:Field = {
		name:'guardianId'
		,controlType: 'Hidden'
		,isRequired: true
		,valueType: 1
		,errorId: 'invalidId'
		,maxValue: 9999999999999
	};
	studentId:Field = {
		name:'studentId'
		,controlType: 'Hidden'
		,isRequired: true
		,valueType: 1
		,errorId: 'invalidId'
		,maxValue: 9999999999999
	};
	instituteId:Field = {
		name:'instituteId'
		,controlType: 'Hidden'
		,valueType: 1
		,errorId: 'invalidTenentKey'
		,maxValue: 9999999999999
	};
	relationType:Field = {
		name:'relationType'
		,controlType: 'Input'
		,label: 'Relationship'
		,isRequired: true
		,listName: 'relationType'
		,valueList: [
			{value:'Mother',text:'Mother'},
			{value:'Father',text:'Father'},
			{value:'Legal Guardian',text:'Legal Guardian'}
			]
		,valueType: 0
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	name:Field = {
		name:'name'
		,controlType: 'Input'
		,label: 'Name'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidName'
		,maxLength: 50
	};
	gender:Field = {
		name:'gender'
		,controlType: 'Dropdown'
		,label: 'Gender'
		,isRequired: true
		,listName: 'gender'
		,valueList: [
			{value:'Male',text:'Male'},
			{value:'Female',text:'Female'},
			{value:'Others',text:'Others'},
			{value:'Not Applicable',text:'Not Applicable'}
			]
		,valueType: 0
		,errorId: 'invalidGender'
		,maxLength: 10
	};
	addressLine1:Field = {
		name:'addressLine1'
		,controlType: 'Input'
		,label: 'Premanent Address - Line 1'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidDesc'
		,maxLength: 1000
	};
	addressLine2:Field = {
		name:'addressLine2'
		,controlType: 'Input'
		,label: 'Premanent Address - Line 2'
		,valueType: 0
		,errorId: 'invalidDesc'
		,maxLength: 1000
	};
	city:Field = {
		name:'city'
		,controlType: 'Input'
		,label: 'Premanent City'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidName'
		,maxLength: 50
	};
	state:Field = {
		name:'state'
		,controlType: 'Dropdown'
		,label: 'Premanent State'
		,isRequired: true
		,listName: 'state'
		,listKey: 'country'
		,keyedList: {
			91 : [
				{value:'Karnataka',text:'Karnataka'}, 
				{value:'Tamil Nadu',text:'Tamil Nadu'}, 
				{value:'Kerala',text:'Kerala'}, 
				{value:'Uttar Pradesh',text:'Uttar Pradesh'}
			], 
			130 : [
				{value:'Karnataka',text:'Karnataka'}, 
				{value:'Tamil Nadu',text:'Tamil Nadu'}, 
				{value:'Kerala',text:'Kerala'}, 
				{value:'Uttar Pradesh',text:'Uttar Pradesh'}
			]
			}
		,valueType: 0
		,errorId: 'invalidState'
		,maxLength: 50
	};
	pincode:Field = {
		name:'pincode'
		,controlType: 'Input'
		,label: 'Premanent Pin Code'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidPin'
		,minLength: 6
		,maxLength: 6
	};
	country:Field = {
		name:'country'
		,controlType: 'Input'
		,label: 'Premanent Country'
		,isRequired: true
		,valueType: 1
		,defaultValue: 130
		,errorId: 'invalidCountry'
		,maxValue: 999
	};
	phoneNumber:Field = {
		name:'phoneNumber'
		,controlType: 'Input'
		,label: 'Phone'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidPhone'
		,maxLength: 20
	};
	email:Field = {
		name:'email'
		,controlType: 'Input'
		,label: 'Email'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidEmail'
		,maxLength: 1000
	};
	occupation:Field = {
		name:'occupation'
		,controlType: 'Input'
		,label: 'Occupation'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	annualSalary:Field = {
		name:'annualSalary'
		,controlType: 'Input'
		,label: 'Annual Salary'
		,isRequired: true
		,valueType: 1
		,errorId: 'invalidIncome'
		,maxValue: 9999999999999
	};
	qualification:Field = {
		name:'qualification'
		,controlType: 'Input'
		,label: 'Qualification'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	createdAt:Field = {
		name:'createdAt'
		,controlType: 'Hidden'
		,valueType: 5
		,errorId: 'invalidTimestamp'
	};
	updatedAt:Field = {
		name:'updatedAt'
		,controlType: 'Hidden'
		,valueType: 5
		,errorId: 'invalidTimestamp'
	};

	public static getInstance(): Guardian {
		return Guardian._instance;
	}

	constructor() {
		super();
		this.fields = new Map();
		this.controls = new Map();
		this.controls.set('guardianId', [Validators.required, Validators.max(9999999999999)]);
		this.controls.set('studentId', [Validators.required, Validators.max(9999999999999)]);
		this.controls.set('instituteId', [Validators.max(9999999999999)]);
		this.controls.set('relationType', [Validators.required, Validators.maxLength(1000)]);
		this.controls.set('name', [Validators.required, Validators.maxLength(50)]);
		this.controls.set('gender', [Validators.required, Validators.maxLength(10)]);
		this.controls.set('addressLine1', [Validators.required, Validators.maxLength(1000)]);
		this.controls.set('addressLine2', [Validators.maxLength(1000)]);
		this.controls.set('city', [Validators.required, Validators.maxLength(50)]);
		this.controls.set('state', [Validators.required, Validators.maxLength(50)]);
		this.controls.set('pincode', [Validators.required, Validators.minLength(6), Validators.maxLength(6), Validators.pattern('[1-9][0-9]{5}')]);
		this.controls.set('country', [Validators.required, Validators.max(999)]);
		this.controls.set('phoneNumber', [Validators.required, Validators.maxLength(20)]);
		this.controls.set('email', [Validators.required, Validators.email, Validators.maxLength(1000)]);
		this.controls.set('occupation', [Validators.required, Validators.maxLength(1000)]);
		this.controls.set('annualSalary', [Validators.required, Validators.max(9999999999999)]);
		this.controls.set('qualification', [Validators.required, Validators.maxLength(1000)]);
		this.controls.set('createdAt', []);
		this.controls.set('updatedAt', []);
		this.listFields = ['relationType', 'gender', 'state'];
		this.keyFields = ['guardianId'];
	}

	public getName(): string {
		 return 'guardian';
	}
}


export interface GuardianData extends Vo {
	relationType?: string, 
	annualSalary?: number, 
	pincode?: string, 
	country?: number, 
	occupation?: string, 
	gender?: string, 
	city?: string, 
	guardianId?: number, 
	studentId?: number, 
	qualification?: string, 
	createdAt?: string, 
	phoneNumber?: string, 
	name?: string, 
	instituteId?: number, 
	addressLine1?: string, 
	addressLine2?: string, 
	state?: string, 
	email?: string, 
	updatedAt?: string
}
