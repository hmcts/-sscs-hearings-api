#HMC to Hearings API
module "servicebus-subscription" {
  source                = "git@github.com:hmcts/terraform-module-servicebus-subscription?ref=master"
  name                  = "hmc-to-sscs-subscription-${var.env}"
  namespace_name        = "hmc-servicebus-${var.env}"
  topic_name            = "hmc-to-cft-${var.env}"
  resource_group_name   = "hmc-shared-${var.env}"
}

data "azurerm_key_vault" "hmc-key-vault" {
  name                = "hmc-${var.env}"
  resource_group_name = "hmc-shared-${var.env}"
}

data "azurerm_key_vault_secret" "hmc-servicebus-connection-string" {
  key_vault_id = data.azurerm_key_vault.hmc-key-vault.id
  name         = "hmc-servicebus-connection-string"
}

resource "azurerm_key_vault_secret" "hmc-servicebus-connection-string" {
  name         = "hmc-servicebus-connection-string"
  value        = data.azurerm_key_vault_secret.hmc-servicebus-connection-string.value
  key_vault_id = data.azurerm_key_vault.sscs_key_vault.id
}

data "azurerm_key_vault_secret" "hmc-servicebus-connection-string-SharedAccessKey" {
  key_vault_id = data.azurerm_key_vault.hmc-key-vault.id
  name         = "hmc-servicebus-connection-string-SharedAccessKey"
}

resource "azurerm_key_vault_secret" "sscs-hmc-servicebus-connection-string-SharedAccessKey" {
  name         = "hmc-servicebus-connection-string-SharedAccessKey"
  value        = data.azurerm_key_vault_secret.hmc-servicebus-connection-string-SharedAccessKey.value
  key_vault_id = data.azurerm_key_vault.sscs_key_vault.id
}

data "azurerm_key_vault_secret" "hmc-servicebus-connection-string-SharedAccessKeyName" {
  key_vault_id = data.azurerm_key_vault.hmc-key-vault.id
  name         = "hmc-servicebus-connection-string-SharedAccessKeyName"
}

resource "azurerm_key_vault_secret" "sscs-hmc-servicebus-connection-string-SharedAccessKeyName" {
  name         = "hmc-servicebus-connection-string-SharedAccessKeyName"
  value        = data.azurerm_key_vault_secret.hmc-servicebus-connection-string-SharedAccessKeyName.value
  key_vault_id = data.azurerm_key_vault.sscs_key_vault.id
}
