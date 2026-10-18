/**
 * Serviço de Integração com a API pública ViaCEP (sem necessidade de autenticação).
 * Realiza a busca automática do logradouro, bairro, cidade e estado a partir do CEP.
 */
export async function buscarEnderecoPorCep(cep) {
  if (!cep) return null;
  const cepLimpo = cep.replace(/\D/g, '');

  if (cepLimpo.length !== 8) {
    return null;
  }

  try {
    const response = await fetch(`https://viacep.com.br/ws/${cepLimpo}/json/`);
    if (!response.ok) return null;

    const data = await response.json();
    if (data.erro) {
      return null;
    }

    return {
      endereco: data.logradouro || '',
      bairro: data.bairro || '',
      cidade: data.localidade || '',
      estado: data.uf || '',
      pais: 'Brasil',
    };
  } catch (error) {
    console.warn('[ViaCEP] Não foi possível consultar o CEP automaticamente:', error.message);
    return null;
  }
}
