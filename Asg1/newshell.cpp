#include <iostream>
#include <sstream>
#include <fstream>
#include <vector>
#include <cstring>
#include <string>
#include <unistd.h>
#include <fcntl.h> 
#include <cstdlib>
#include <fstream>
#include <algorithm>
#include <sys/signal.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>
#include <stdlib.h>

#define MAX_PIPE 3

using namespace std;

//----------------------------------------------------------------------
// Vetores de PIDs de filhos em background e foreground para job control
bool cont = false;
vector<pid_t> bg_sons;
vector<pid_t> fg_sons;
vector<char*>* p_pipe[MAX_PIPE];

//----------------------------------------------------------------------
// Dada uma string str com vários tokens separados por separadores sep
// retorna um vector de char* contendo cada um dos tokens. Destroi a
// string inicial no processo.
// * Parâmetros:
//		string str - String a ser divida
//		char sep - String contendo o separador dos tokens
// * Retorno:
//		vector<char*>* apontando para vetor de tokens.
vector<char*>* parse_args(string str, char sep)
{
	vector<char*>* res = new vector<char*>();
	stringstream lStream(str);
	string celula;	
	if(lStream.str().empty()) return NULL;
	while(getline(lStream,celula,sep))
  {
		if(celula.empty()) continue;
		if(celula.at(0) == ' ') celula.erase(0,1);
		char* ca = new char[celula.size()+1];
		copy(celula.begin(), celula.end(), ca);
		ca[celula.size()] = '\0';
		res->push_back(ca);
	}
	return res;
}
//----------------------------------------------------------------------


//----------------------------------------------------------------------
// Dado um PID pid, retorna os argumentos utilizados em sua inicialização
// * Parâmetros:
//		pid_t pid - PID do processo do qual se deseja os argumentos.
// * Retorno:
//		string contendo os argumentos de inicialização do processo.

string proc_cmdline(int pid)
{
    char filename[100];
    string line;
    sprintf(filename, "/proc/%d/cmdline", pid);
    ifstream procfile (filename);
	stringstream res;
	if (procfile.is_open())
		while(getline(procfile, line, '\0'))
			res << " " << line;
	return res.str();
}
//----------------------------------------------------------------------

//----------------------------------------------------------------------
// Dado um PID pid, retorna o status do processo
// * Parâmetros:
//		pid_t pid - PID do processo do qual se deseja os argumentos.
// * Retorno:
//		string contendo os argumentos de inicialização do processo.
string proc_status(int pid)
{
    char filename[100];
    string line;
    sprintf(filename, "/proc/%d/stat", pid);
    ifstream procfile (filename);
	stringstream res;
	for(int i=0; i<3; i++)
		getline(procfile, line, ' ');
	return line;
}

//----------------------------------------------------------------------
// Tratador de sinais da shell
// * Parâmetros:
//		int sig - Signal a ser tratado
// * Sinais Tratados:
//		SIGINT - Mata todos os filhos em foreground.
//		SIGTSTP - Pausa todos os filhos.
void SIG_HANDLER(int sig)
{
	switch(sig)
	{
		case SIGINT:
			cout << endl << "<Ctrl-c pressionado>" << endl << "Processos finalizados:" << endl;
			for(pid_t son : fg_sons)
			{
				string cmdline = proc_cmdline(son);
				kill(son,SIGKILL);
				waitpid(son,NULL, WNOHANG);
				cout << son << " - \"" << cmdline << "\"" << endl;
			}
			cout << "--" << endl;
			break;
		case SIGTSTP:
			cout << endl << "<Ctrl-z pressionado>" << endl << "Processos suspensos:" << endl;
			for(pid_t son : bg_sons)
			{
				string cmdline = proc_cmdline(son);
				kill(son,SIGSTOP);
				cout << son << " - \"" << cmdline << "\"" << endl;
			}
			for(pid_t son : fg_sons)
			{
				string cmdline = proc_cmdline(son);
				kill(son,SIGSTOP);
				cont = true;
				cout << son << " - \"" << cmdline << "\"" << endl;
			}
			cout << "--" << endl;
			break;
	}
}
//----------------------------------------------------------------------


//----------------------------------------------------------------------
// Limpa os vetores de filhos bg_sons e fg_sons, removendo filhos
// que já tenham terminando a execução.
// TODO ajeitar a condição de remoção (WIFSIGNAL)
void clear_vector()
{
	for (vector<pid_t>::iterator b = bg_sons.begin(); b != bg_sons.end();)
	{ 
		if (proc_cmdline(*b).empty())
		{
			waitpid(*b, NULL, WNOHANG);
			b = bg_sons.erase( b );
		}	
		else
			++b;
	}
	for (vector<pid_t>::iterator b = fg_sons.begin(); b != fg_sons.end();)
	{ 
		if (proc_cmdline(*b).empty())
		{
			waitpid(*b, NULL, WNOHANG);
			b = fg_sons.erase( b );
		}
		else
			++b;
	}
}


//----------------------------------------------------------------------


//----------------------------------------------------------------------
// Trata os comandos internos da shell.
// * Comandos Tratados:
//		exit - Chama exit(0)
//		echo - Repete a string passada.
//		pwd - Retorna o diretório atual.
//		cd - Altera o diretório atual.
//		clean.
//		wait.
int defaultcmds(vector<char*>* args)
{
	if(args == NULL) return 1;
	char* cmd = args->at(0);
	if(cmd == NULL) return 1;
	else if(!strcmp(cmd,"exit"))
	{
		for(pid_t son : bg_sons)
		{
			kill(son, SIGKILL);
			waitpid(son, NULL, WNOHANG);
		}
		for(pid_t son : fg_sons)
		{
			kill(son, SIGKILL);
			waitpid(son, NULL, WNOHANG);
		}
		exit(0);
	}
	else if(!strcmp(cmd,"echo"))
	{
		for(int i = 1; args->at(i) != NULL; i++)
				cout << args->at(i) << " ";
		cout << endl;
		return 1;
	}
	else if(!strcmp(cmd,"pwd"))
	{
		char curDir[100];
		getcwd(curDir, 100);
		cout << curDir << endl;	
		return 1;
	}
	else if(!strcmp(cmd,"cd"))
	{
		chdir(args->at(1));
		return 1;
	}
	else if(!strcmp(cmd,"clean"))
	{
		clear_vector();
		return 1;
	}
	else if(!strcmp(cmd,"wait"))
	{
		int status;
		while(1)
		{
			for (vector<pid_t>::iterator son = bg_sons.begin(); son != bg_sons.end();)
			{ 
				if(waitpid(*son, &status, WNOHANG) > 0)
				{
					cout << *son << " RETORNOU " << status << endl;
					son = bg_sons.erase(son);
				}	
				else
					++son;
			}			
			for (vector<pid_t>::iterator son = fg_sons.begin(); son != fg_sons.end();)
			{ 
				if(waitpid(*son, &status, WNOHANG) > 0)
				{
					cout << *son << " RETORNOU " << status << endl;
					son = bg_sons.erase(son);
				}	
				else
					++son;
			}
			if(bg_sons.size() + fg_sons.size() == 0) break;
		}
		return 1;
	}
	else return 0;
}
//----------------------------------------------------------------------


//----------------------------------------------------------------------
// Cria um novo processo com os parâmetros passados
// * Parâmetros:
//		vector<char*>* - Vector contendo os argumentos do processo
// * Retorno:
//		int - Indicando sucesso (1) ou fracasso (0)
int executa(vector<char*>* args, int *pipe_in, int *pipe_out, bool redirect_in, bool redirect_out)
{
		pid_t pid;
		
		bool bg = false;

		if(args->size() > 2 && !strcmp(args->at(args->size()-2), "&")) 
			bg = true;
		
		if((pid = fork()) < 0)
		{
			cout << "Erro no fork!" << endl;
			return 0;
		} 
		else if(pid == 0)
		{
			if(bg)
			{
				setsid();
				args->erase(args->begin()+args->size()-2);
			}

			signal(SIGINT, SIG_IGN);
			signal(SIGTSTP, SIG_IGN);

			char ** cmd = args->data();

			if(redirect_in)
		 		dup2(pipe_in[0], STDIN_FILENO);

			if(redirect_out)
				dup2(pipe_out[1], STDOUT_FILENO);


			if(redirect_in || redirect_out)	
			{
				close(pipe_in[1]);
		 		close(pipe_in[0]);
				close(pipe_out[1]);
		 		close(pipe_out[0]);
			}

			execvp(cmd[0], cmd);
			perror("execvp");
			exit(1);
		}
		else
		{ 
			if(bg)
			{
				bg_sons.push_back(pid);
				cout << "[" << bg_sons.size() << "] " << pid << endl;
			}
			else
			{
				fg_sons.push_back(pid);
				if(redirect_in == false && redirect_out == false)
					waitpid(pid, NULL, WUNTRACED);
			}
		}
		delete args;
		return 1;
}


//----------------------------------------------------------------------
//Cria um vetor de instruções. Cada instrução sera tera sua saida redirecionada
void parser_pipes(vector<char*>* args)
{
		int count = 0;
		for (char* it : *args)
		{
				if(it != NULL)
				{	
						string c1 = string(it);
						vector<char*>* temp = parse_args(c1, ' ');
						temp->push_back(NULL);
						p_pipe[count++] = temp;
				}
		}
}
	
//----------------------------------------------------------------------
// Executa n processos, redirecionando a saida de um para a entrada de outro
void executa_pipes(int p[][2], int tam)
{
	if(pipe(p[0]))
		printf("Erro 0\n");

	if(pipe(p[1]))
		printf("Erro 1\n");

	if(tam == 3)
	{	
		if(!executa(p_pipe[0], p[1], p[0], false, true)) cout << "Falha na criação do processo!" << endl;
		if(!executa(p_pipe[1], p[0], p[1], true, true)) cout << "Falha na criação do processo!" << endl;
		if(!executa(p_pipe[2], p[1], p[0], true, false)) cout << "Falha na criação do processo!" << endl;
	}
	else if(tam == 2)
	{
		if(!executa(p_pipe[0], p[1], p[0], false, true)) cout << "Falha na criação do processo!" << endl;
		if(!executa(p_pipe[1], p[0], p[1], true, false)) cout << "Falha na criação do processo!" << endl;
	}

	close(p[0][0]);
	close(p[0][1]);
	close(p[1][0]);
	close(p[1][1]);

	for(pid_t son : fg_sons)
		waitpid(son, NULL, 0);

}

//----------------------------------------------------------------------
// Função main. Fica em loop infinito recebendo argumentos para criação
// de novos processos.
// * Argumentos de execução:
//		Arquivo contendo os comandos a serem passados para a shell
int main(int argc, char *argv[])
{
	const int MAX = 100;
	char curDir[MAX], buffer[MAX];
	int p[2][2];
	
	signal(SIGINT, SIG_HANDLER);
	signal(SIGTSTP, SIG_HANDLER);
	
	ifstream file(argv[1]);

	vector<char*>* args = NULL;

	while(1)
	{
		clear_vector();
		if(!file)
		{	
			string input;
			getcwd(curDir, 100);
			cout << getlogin() << "@" << curDir << "% ";
			getline(cin, input);
			args = parse_args(input, '|');
		}
		else
		{
			file.getline(buffer, MAX); // Lê uma linha do arquivo 
 			args = parse_args(buffer, '|');
		}	
		if(args == NULL) continue;
		if((args->size()) > 1)
		{
			parser_pipes(args);
			executa_pipes(p, args->size());
		}
		else
		{
			if(args->at(0) !=  NULL)
			{
				vector<char*>* args2 = parse_args(args->at(0), ' ');
				args2->push_back(NULL);				
				if(defaultcmds(args2)) continue;
				if(!executa(args2, NULL, NULL, false, false)) cout << "Falha na criação do processo!" << endl;
			}		
		}
	}
	return 0;
}
//----------------------------------------------------------------------
