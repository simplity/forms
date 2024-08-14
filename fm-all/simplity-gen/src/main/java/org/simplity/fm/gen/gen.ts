import {allListSources} from './allListSources';
import {allMessages} from './allMessages';
import {allValueSchemas} from './allValueSchemas';
import {allForms} from './allForms';

export type ListSourceName = keyof typeof allListSources;
export type MessageName = keyof typeof allMessages;
export type ValueSchemaName = keyof typeof allValueSchemas;
export type FormName = keyof typeof allForms;

export const generatedArtifacts = {
  allListSources,
  allMessages,
  allValueSchemas,
  allForms,
};