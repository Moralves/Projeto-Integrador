export interface Usuario {
  id?: number;
  nome: string;
  email: string;
  role?: string;
  senha?: string;
}

export interface Ambulancia {
  id?: number;
  placa: string;
  latitude?: number;
  longitude?: number;
  status?: string;
  tipo?: string;
}

export interface Ocorrencia {
  id?: number;
  descricao: string;
  endereco: string;
  latitude: number;
  longitude: number;
  status: string;
  dataCriacao?: string | Date;
  idUsuario?: number;
  prioridade?: string;
}

export interface Equipe {
  id?: number;
  nome: string;
  profissionais?: Profissional[];
}

export interface Profissional {
  id?: number;
  nome: string;
  cargo: string;
}

export interface Atendimento {
  id?: number;
  idOcorrencia: number;
  idAmbulancia: number;
  status: string;
  dataDespacho?: string | Date;
  dataRetorno?: string | Date;
}
